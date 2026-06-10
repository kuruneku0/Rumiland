package com.example.utils

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletPoint(val text: String) : MarkdownBlock()
    data class QuoteBlock(val text: String) : MarkdownBlock()
}

object MarkdownParser {
    fun parse(markdownText: String): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        val lines = markdownText.split("\n")
        var inCodeBlock = false
        var currentCodeLanguage = ""
        val currentCodeBuilder = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // End of code block
                    blocks.add(MarkdownBlock.CodeBlock(currentCodeLanguage, currentCodeBuilder.toString().trimEnd()))
                    inCodeBlock = false
                    currentCodeBuilder.clear()
                    currentCodeLanguage = ""
                } else {
                    // Start of code block
                    inCodeBlock = true
                    currentCodeLanguage = trimmed.removePrefix("```").trim()
                }
                continue
            }

            if (inCodeBlock) {
                currentCodeBuilder.append(line).append("\n")
                continue
            }

            // Headers
            if (trimmed.startsWith("###")) {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("###").trim()))
                continue
            } else if (trimmed.startsWith("####")) {
                blocks.add(MarkdownBlock.Header(4, trimmed.removePrefix("####").trim()))
                continue
            } else if (trimmed.startsWith("##")) {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("##").trim()))
                continue
            } else if (trimmed.startsWith("#")) {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("#").trim()))
                continue
            }

            // Bullet points
            if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val bulletText = if (trimmed.startsWith("-")) trimmed.removePrefix("-").trim() else trimmed.removePrefix("*").trim()
                blocks.add(MarkdownBlock.BulletPoint(bulletText))
                continue
            }

            // Block Quotes
            if (trimmed.startsWith(">")) {
                blocks.add(MarkdownBlock.QuoteBlock(trimmed.removePrefix(">").trim()))
                continue
            }

            // Normal paragraphs, but ignore excessive blank lines
            if (trimmed.isNotEmpty()) {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }

        // Just in case the code block isn't properly closed
        if (inCodeBlock && currentCodeBuilder.isNotEmpty()) {
            blocks.add(MarkdownBlock.CodeBlock(currentCodeLanguage, currentCodeBuilder.toString().trimEnd()))
        }

        return blocks
    }
}
