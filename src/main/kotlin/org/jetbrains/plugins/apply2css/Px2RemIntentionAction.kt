package org.jetbrains.plugins.apply2css

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class Px2RemIntentionAction : IntentionAction {
    // 顯示的選單名稱
    override fun getText(): String = "Parse px to rem"

    // 分類名
    override fun getFamilyName(): String = "MyCustomIntentionFamily"

    // 顯示條件
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return Regex("(\\s+)?(\\dpx)").containsMatchIn(getDocumentCurrentLineContent(editor))
    }

    // 執行
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val application = ApplicationManager.getApplication()

        // 該階段僅跳出選單，尚未案下確認
        if (!application.isWriteAccessAllowed) return

        // 設定
        val settings = project.getService(ProjectSettingsService::class.java)

        // 最終內容
        val lineText = getDocumentCurrentLineContent(editor)
        val result =  lineText.replace(Regex("(\\d+)px")) { matchResult ->
            val matchedValue = matchResult.groupValues[1].toFloat() // 將匹配到的數字轉換為整數
            val remValue = String.format("%.2f", matchedValue * settings.unitRate.toFloat()).toDouble() // 單位由設定中決定
            "${remValue}rem" // 構建新的字符串，包括 rem 單位
        }
        writeCurrentLineContent(project, editor, result.replace(Regex("\\.0+rem"), "rem").replace(Regex("\\s0rem"), " 0"))
    }

    override fun startInWriteAction(): Boolean = true
}

// 取得當前行的內容
private fun getDocumentCurrentLineContent(editor: Editor): String {
    val document = editor.document
    val lineNumber = document.getLineNumber(editor.caretModel.offset)
    return document.text.substring(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))
}

// 覆寫當前行內容
private fun writeCurrentLineContent(project: Project, editor: Editor, content: String) {
    // 處理腳本輸出
    WriteCommandAction.runWriteCommandAction(project) {
        val document = editor.document
        val lineNumber = document.getLineNumber(editor.caretModel.offset)
        document.replaceString(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber), content)
    }
}
