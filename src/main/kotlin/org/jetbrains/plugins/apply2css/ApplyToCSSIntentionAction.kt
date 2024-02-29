package org.jetbrains.plugins.apply2css

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files

val Reg = Regex(";${'$'}") // 過濾最後一個";"

class ApplyToCSSIntentionAction : IntentionAction {
    // 顯示的選單名稱
    override fun getText(): String = "Parse @apply to Pure CSS"

    // 分類名
    override fun getFamilyName(): String = "MyCustomIntentionFamily"

    // 顯示條件
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return Regex("(\\s+)?(@|//)").containsMatchIn(getDocumentCurrentLineContent(editor))
    }

    // 執行
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val application = ApplicationManager.getApplication()

        // 該階段僅跳出選單，尚未案下確認
        if (!application.isWriteAccessAllowed) return

        // 內容暫存到檔案中
        val lineText = getDocumentCurrentLineContent(editor)
        val tmpFile = createTempCSSFileWithContent(lineText)

        try {
            // config
            val settings = project.getService(ProjectSettingsService::class.java)
            val configPath = settings.configPath

            // 要執行的命令
            val interpreterManager = NodeJsInterpreterManager.getInstance(project)
            val nodePath = File(interpreterManager.interpreter.toString()).parent
            var command = "PATH=\$PATH:${nodePath} tailwindcss -i ${tmpFile.path}" // 例子：列出目錄內容
            // config
            if (configPath.isNotEmpty()) {
                command += " -c ${configPath}"
            }

            // 創建 ProcessBuilder
            val builder = ProcessBuilder()
            builder.command("bash", "-c", command)
            val process = builder.start()

            // 讀取命令的輸出
            val lines = BufferedReader(InputStreamReader(process.inputStream)).readLines()

            // 最終 @apply 內容
            val result = mutableListOf("")

            // 打印每行輸出
            if (lines.isNotEmpty()) {
                val mutableList = lines.map { item -> item.trim() }.toMutableList()
                val tmpIndex = mutableList.mapIndexed { index, item ->
                    if (item == ".tmp {") return@mapIndexed index
                    else return@mapIndexed -1
                }.filter { item -> item > -1 }

                tmpIndex.forEach { index ->
                    mutableList[index] = "";
                    mutableList.forEachIndexed { idx, item ->
                        if (item != "}" || idx <= index) return@forEachIndexed
                        mutableList[idx] = "";
                        return@forEach
                    }
                }

                // 前面空白數量
                val leadingSpaces = lineText.takeWhile { it.isWhitespace() }
                mutableList.forEach { item ->
                    if (item.isEmpty()) return@forEach
                    // 括號後面不需要分號
                    if (item.endsWith("}") || item.endsWith("{")) {
                        result.add("${leadingSpaces}${item.trim().replace(Reg, "")}")
                    }
                    // 其他屬性後面需要分號
                    else {
                        result.add("${leadingSpaces}${item.trim().replace(Reg, "")};")
                    }
                }
            }

            // 結果
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                thisLogger().warn("Process code $exitCode")
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).readLines().toMutableList()
                var message = stderr.joinToString("")
                if (message.contains("CssSyntaxError")) {
                    val errors = stderr.filter { it.contains("CssSyntaxError") }
                    message = Regex(".+tmp:\\d:\\d:(.*)").replace(errors.joinToString(""), "$1").trim()
                }
                Messages.showMessageDialog(project,
                    message,
                    "Command Line Error",
                    Messages.getInformationIcon())
                return
            }

            writeCurrentLineContent(project, editor, result.filter { it.isNotBlank() }.joinToString("\n"))
        } catch (e: Exception) {
            e.printStackTrace()
            thisLogger().warn("Fail")
        } finally {
            tmpFile.delete()
        }
    }

    override fun startInWriteAction(): Boolean = true
}

// 取得當前行的內容
private fun getDocumentCurrentLineContent(editor: Editor): String {
    val document = editor.document
    val lineNumber = document.getLineNumber(editor.caretModel.offset)
    return document.text.substring(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))
}

// 生成 CSS @apply 內容到暫存區
private fun createTempCSSFileWithContent(content: String): File {
    val tempFilePath = Files.createTempFile("applyTmpContent", null)
    Files.write(tempFilePath, (".tmp {\n\t${content.replace(Regex("(\\s+)?(@(apply)?|//)"), "@apply").replace(Reg, "")};\n}").toByteArray())
    return tempFilePath.toFile()
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