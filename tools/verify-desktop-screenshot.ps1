param(
    [int]$WaitSeconds = 12,
    [string]$OutputDirectory = "build/verification/screenshots",
    [string]$JavaHome = "",
    [switch]$KeepOpen
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $projectRoot "gradlew.bat"
$outputPath = Join-Path $projectRoot $OutputDirectory
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$screenshotPath = Join-Path $outputPath "desktop-$timestamp.png"
$logPath = Join-Path $outputPath "desktop-$timestamp.log"

if (-not (Test-Path -LiteralPath $gradleWrapper)) {
    throw "Gradle wrapper was not found: $gradleWrapper"
}

New-Item -ItemType Directory -Path $outputPath -Force | Out-Null

$javaHomeCandidates = @(
    $JavaHome
    $env:JAVA_HOME
    "D:\java"
) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }

$resolvedJavaHome = $null
foreach ($candidate in $javaHomeCandidates) {
    $candidateJava = Join-Path $candidate "bin\java.exe"
    if (Test-Path -LiteralPath $candidateJava) {
        $resolvedJavaHome = $candidate
        break
    }
}

if ($null -eq $resolvedJavaHome) {
    throw "JDK 21 was not found. Set -JavaHome or JAVA_HOME to a JDK 21 directory."
}

$env:JAVA_HOME = $resolvedJavaHome
$env:Path = "$(Join-Path $resolvedJavaHome 'bin');$env:Path"
$javaPath = Join-Path $resolvedJavaHome "bin\java.exe"
$javaVersionOutput = (& $env:ComSpec /c "`"$javaPath`" -version 2>&1" | Out-String)
if ($javaVersionOutput -notmatch 'version "21(?:[."]|$)') {
    throw "JDK 21 is required. Detected: $($javaVersionOutput.Trim())"
}

Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;

public static class DesktopScreenshotNative
{
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    [StructLayout(LayoutKind.Sequential)]
    public struct RECT
    {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }

    [DllImport("user32.dll")]
    public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool IsWindowVisible(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern int GetWindowText(IntPtr hWnd, System.Text.StringBuilder text, int maxCount);

    [DllImport("user32.dll")]
    public static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

    [DllImport("user32.dll")]
    public static extern bool EnumWindows(EnumWindowsProc callback, IntPtr lParam);

    public static IntPtr[] GetVisibleWindowsWithTitles()
    {
        var windows = new System.Collections.Generic.List<IntPtr>();
        EnumWindows((hWnd, lParam) =>
        {
            if (IsWindowVisible(hWnd) && GetWindowTextLength(hWnd) > 0)
            {
                windows.Add(hWnd);
            }
            return true;
        }, IntPtr.Zero);
        return windows.ToArray();
    }

    public static string GetTitle(IntPtr hWnd)
    {
        var text = new System.Text.StringBuilder(GetWindowTextLength(hWnd) + 1);
        GetWindowText(hWnd, text, text.Capacity);
        return text.ToString();
    }

    public static int GetProcessId(IntPtr hWnd)
    {
        uint processId;
        GetWindowThreadProcessId(hWnd, out processId);
        return (int)processId;
    }
}
"@

$existingWindowHandles = @([DesktopScreenshotNative]::GetVisibleWindowsWithTitles())

$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = $env:ComSpec
$startInfo.Arguments = "/d /c call `"$gradleWrapper`" :desktopApp:run --no-daemon --console=plain > `"$logPath`" 2>&1"
$startInfo.WorkingDirectory = $projectRoot
$startInfo.UseShellExecute = $false
$startInfo.CreateNoWindow = $true

$launcher = New-Object System.Diagnostics.Process
$launcher.StartInfo = $startInfo
[void]$launcher.Start()

try {
    $windowHandle = [IntPtr]::Zero
    $windowProcess = $null
    $deadline = (Get-Date).AddSeconds($WaitSeconds)
    while ((Get-Date) -lt $deadline -and $windowHandle -eq [IntPtr]::Zero) {
        Start-Sleep -Milliseconds 500
        $windowHandle = @([DesktopScreenshotNative]::GetVisibleWindowsWithTitles()) |
            Where-Object { $_ -notin $existingWindowHandles } |
            Select-Object -First 1
        if ($null -eq $windowHandle) {
            $windowHandle = [IntPtr]::Zero
        }
    }

    if ($windowHandle -eq [IntPtr]::Zero) {
        if ($launcher.HasExited) {
            throw "Desktop app did not start. See log: $logPath"
        }
        throw "Desktop window was not detected within $WaitSeconds seconds. Run this script from an interactive Windows desktop session. See log: $logPath"
    }

    $windowProcessId = [DesktopScreenshotNative]::GetProcessId($windowHandle)
    $windowProcess = Get-Process -Id $windowProcessId -ErrorAction SilentlyContinue

    [void][DesktopScreenshotNative]::SetForegroundWindow($windowHandle)
    Start-Sleep -Milliseconds 500

    $rect = New-Object DesktopScreenshotNative+RECT
    if (-not [DesktopScreenshotNative]::GetWindowRect($windowHandle, [ref]$rect)) {
        throw "Could not read the Desktop window bounds."
    }

    $width = $rect.Right - $rect.Left
    $height = $rect.Bottom - $rect.Top
    if ($width -lt 200 -or $height -lt 120) {
        throw "Desktop window bounds are too small: ${width}x${height}"
    }

    $bitmap = New-Object System.Drawing.Bitmap($width, $height)
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.CopyFromScreen(
                $rect.Left,
                $rect.Top,
                0,
                0,
                [System.Drawing.Size]::new($width, $height)
            )
        } finally {
            $graphics.Dispose()
        }
        $bitmap.Save($screenshotPath, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $bitmap.Dispose()
    }

    Write-Output "Screenshot: $screenshotPath"
    Write-Output "Window: $([DesktopScreenshotNative]::GetTitle($windowHandle)) [$($width)x$($height)]"
    Write-Output "Log: $logPath"

    if ($KeepOpen) {
        Write-Output "Desktop app remains open because -KeepOpen was specified."
    }
} finally {
    if (-not $KeepOpen) {
        if ($windowProcess -and -not $windowProcess.HasExited) {
            Stop-Process -Id $windowProcess.Id -Force -ErrorAction SilentlyContinue
        }
        if ($launcher -and -not $launcher.HasExited) {
            Stop-Process -Id $launcher.Id -Force -ErrorAction SilentlyContinue
        }
    }
}
