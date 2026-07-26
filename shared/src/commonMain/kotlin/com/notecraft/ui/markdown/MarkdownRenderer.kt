package com.notecraft.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownContent(
    content: String,
    fontSize: Int = 14,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { MarkdownParser.parse(content) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val codeBg = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        for (block in blocks) {
            when (block) {
                is MdBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    }
                    val annotated = buildMdAnnotated(block.text, textColor, primaryColor, codeBg, fontSize)
                    Text(
                        text = annotated,
                        style = style,
                        modifier = Modifier.padding(top = if (block.level <= 2) 16.dp else 12.dp, bottom = 8.dp)
                    )
                }

                is MdBlock.Paragraph -> {
                    val annotated = buildMdAnnotated(block.spans, textColor, primaryColor, codeBg, fontSize)
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MdBlock.CodeBlock -> {
                    Surface(
                        color = codeBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (block.language.isNotEmpty()) {
                                Text(
                                    text = block.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text = block.code,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = (fontSize - 1).sp,
                                    color = textColor.copy(alpha = 0.85f),
                                    lineHeight = (fontSize * 1.5).sp
                                )
                            }
                        }
                    }
                }

                is MdBlock.BulletList -> {
                    for ((idx, item) in block.items.withIndex()) {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            val prefix = if (block.ordered) "${idx + 1}." else "\u2022"
                            Text(prefix + " ", fontSize = fontSize.sp,
                                color = textColor, fontWeight = FontWeight.Bold)
                            Text(
                                text = buildMdAnnotated(item, textColor, primaryColor, codeBg, fontSize),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                is MdBlock.TaskList -> {
                    for (item in block.items) {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            val checkText = if (item.checked) "\u2611" else "\u2610"
                            Text(checkText + " ", fontSize = (fontSize + 2).sp)
                            Text(
                                text = buildMdAnnotated(item.spans, textColor, primaryColor, codeBg, fontSize),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                is MdBlock.Blockquote -> {
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(primaryColor.copy(alpha = 0.3f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = buildMdAnnotated(block.text, textColor, primaryColor, codeBg, fontSize),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp),
                            color = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MdBlock.Table -> {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row {
                            block.headers.forEach { h ->
                                Text(
                                    text = h,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (fontSize - 1).sp,
                                    modifier = Modifier.width(100.dp).padding(4.dp)
                                )
                            }
                        }
                        HorizontalDivider()
                        block.rows.forEach { row ->
                            Row {
                                row.forEachIndexed { idx, cell ->
                                    Text(
                                        text = cell,
                                        fontSize = (fontSize - 1).sp,
                                        modifier = Modifier.width(if (idx < block.headers.size) 100.dp else 80.dp).padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                is MdBlock.ThematicBreak -> {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

private fun buildMdAnnotated(
    spans: List<MdSpan>,
    textColor: Color,
    linkColor: Color,
    codeBg: Color,
    fontSize: Int
) = buildAnnotatedString {
    for (span in spans) {
        if (span.code) {
            withStyle(SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = (fontSize - 1).sp,
                background = codeBg,
                color = textColor
            )) { append(span.text) }
        } else if (span.link != null) {
            withStyle(SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline
            )) { append(span.link.text) }
        } else {
            var style = SpanStyle(fontSize = fontSize.sp, color = textColor)
            if (span.bold) style = style.copy(fontWeight = FontWeight.Bold)
            if (span.italic) style = style.copy(fontStyle = FontStyle.Italic)
            withStyle(style) { append(span.text) }
        }
    }
}
