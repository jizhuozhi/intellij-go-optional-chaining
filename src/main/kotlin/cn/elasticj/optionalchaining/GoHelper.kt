package cn.elasticj.optionalchaining

import com.goide.psi.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.ResolveState
import com.intellij.psi.util.parentOfType
import java.lang.IllegalArgumentException

class GoHelper {

    companion object {
        private fun getCaretPsiElement(editor: Editor): PsiElement? {
            if (editor !is EditorEx) {
                return null
            }

            val project = editor.project ?: return null
            val psiFile = PsiManager.getInstance(project).findFile(editor.virtualFile)
            val caretModel = editor.caretModel
            return psiFile?.findElementAt(caretModel.offset)
        }

        private fun getCaretFunctionDeclaration(editor: Editor): GoFunctionDeclaration? {
            val psiElement = getCaretPsiElement(editor)
            return psiElement?.parentOfType<GoFunctionDeclaration>()
        }

        private fun getCaretFunctionLit(editor: Editor): GoFunctionLit? {
            val psiElement = getCaretPsiElement(editor)
            return psiElement?.parentOfType<GoFunctionLit>()
        }

        private fun getGoNearestFunctionResult(editor: Editor): GoResult? {
            val functionLit = getCaretFunctionLit(editor)
            if (functionLit != null) {
                return functionLit.result
            }

            val functionDeclaration = getCaretFunctionDeclaration(editor)
            return functionDeclaration?.signature?.result
        }

        fun getCaretFunctionResultTypes(editor: Editor): List<GoType?> {
            val result = getGoNearestFunctionResult(editor) ?: return emptyList()
            return if (result.type != null) {
                listOf(result.type)
            } else if (result.parameters != null) {
                result.parameters?.parameterDeclarationList?.map { it.type } ?: emptyList()
            } else {
                emptyList()
            }
        }

        fun defaultValue(goType: GoType): String {
            when (goType.getUnderlyingType(ResolveState.initial())) {
                is GoPointerType, is GoInterfaceType, is GoArrayOrSliceType, is GoMapType -> {
                    return "nil"
                }

                is GoStructType -> {
                    val structName = goType.presentationText
                    return "${structName}{}"
                }

                else -> return when (goType.presentationText) {
                    "bool" -> "false"
                    "string" -> "\"\""
                    "int", "int8", "int16", "int32", "int64" -> "0"
                    "uint", "uint8", "uint16", "uint32", "uint64", "uintptr" -> "0"
                    "byte", "rune" -> "0"
                    "float32", "float64" -> "0"
                    "complex64", "complex128" -> "0"
                    else -> {
                        throw IllegalArgumentException("Unsupported GoType: ${goType.presentationText}")
                    }
                }
            }

        }
    }
}