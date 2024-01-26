package cn.elasticj.optionalchaining

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys.EDITOR
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

class OptionalChainingAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PROJECT)
        val editor = e.getData(EDITOR) ?: return
        val document = editor.document

        val extension = FileDocumentManager.getInstance().getFile(document)?.extension
        if ((extension == null) || (extension.lowercase() != "go")) {
            return
        }

        if (!document.isWritable) {
            return
        }

        val selectionModel = editor.selectionModel
        val lineNumber = document.getLineNumber(selectionModel.selectionEnd)

        val textRange = TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))

        val selectedLine = document.text.substring(textRange.startOffset, textRange.endOffset)

        val expr = getSelectedExpr(selectedLine)
        if (expr == "") {
            return
        }

        val resultTypes = GoHelper.getCaretFunctionResultTypes(editor)
        if (resultTypes.isEmpty() || resultTypes.size > 2) {
            // TODO error handling
            return
        }

        val exprNode = ExprParer.parse(expr)

        val code = CodeRenderer.render(exprNode,GoHelper.defaultValue(resultTypes[0]!!), when (resultTypes.size) {
            1 -> false
            2 -> true
            else -> return
        } )

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(textRange.endOffset, code)
        }

    }

    private fun getSelectedExpr(selectedText: String): String {
        val matcher = pattern.matcher(selectedText)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return ""
    }

    companion object {
        val pattern = Pattern.compile("// ?optional (.*)")!!
    }
}