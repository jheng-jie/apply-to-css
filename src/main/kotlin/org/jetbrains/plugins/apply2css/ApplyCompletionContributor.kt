package org.jetbrains.plugins.apply2css

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.IconLoader
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class DemoCompletionContributor : CompletionContributor() {
    init {
        val myIcon = IconLoader.getIcon("intentionDescriptions/ApplyToCSSIntentionAction/atom-16.png", this::class.java)

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
                val currentLine = getCurrentLineContent(parameters.editor)
                if (!(currentLine.startsWith("//") || currentLine.startsWith("@"))) return

                val firstChar: String = Regex(".+\\s(.*)").replace(currentLine, "$1")
                if (firstChar.trim().isEmpty()) return

                // 字首
                val firstKeywords = ApplyConstant.filterByFirstChar(firstChar[0])
                if (!firstKeywords.isNullOrEmpty()) {
                    firstKeywords.forEach { item ->
                        resultSet.addElement(LookupElementBuilder.create(item.key)
                                .withIcon(myIcon)
                                .withTypeText(item.value, true) // .withTailText(" (remark)", true)
                        )
                    }
                }

                // 關鍵字
                val keywords = ApplyConstant.filterByKeyword(firstChar)
                if (keywords.isNotEmpty()) {
                    keywords.forEach { item ->
                        resultSet.addElement(LookupElementBuilder.create(item.key)
                                .withIcon(myIcon)
                                .withTypeText(item.value, true) // .withTailText(" (remark)", true)
                        )
                    }
                }
                // resultSet.restartCompletionOnAnyPrefixChange()
            }

            private fun getCurrentLineContent(editor: Editor): String {
                val document = editor.document
                val lineNumber = document.getLineNumber(editor.caretModel.offset)
                return document.text.substring(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber)).trim()
            }
        })
    }
}
