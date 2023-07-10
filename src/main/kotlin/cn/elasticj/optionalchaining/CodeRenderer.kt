package cn.elasticj.optionalchaining

class CodeRenderer {

    companion object {
        fun render(exprNode: ExprParer.ExprNode, defaultValue: String, hasError: Boolean): String {
            val stringBuilder = StringBuilder()
            val fragments = exprNode.fragments
            stringBuilder.append("\n")
            fragments.forEachIndexed { index, fragment ->
                val currentIndex = "_${index}"

                val statement = if (index == 0) {
                    fragment.statement
                } else {
                    "_${index - 1}.${fragment.statement}"
                }

                if (index == fragments.size - 1) {
                    // "return $statement [, nil]"
                    stringBuilder.append("return ").append(statement)
                    if (hasError && !fragment.hasError) {
                        stringBuilder.append(", nil")
                    }
                    stringBuilder.append("\n")
                } else {
                    // """
                    // _$index [, err] := $statement
                    // [ when fragment hasError and hasOptional
                    // if err != nil || _$index == nil {
                    //     return $defaultValue, err
                    // }
                    // ]
                    // [ when fragment hasError
                    // if err != nil {
                    //     return $defaultValue, err
                    // }
                    // ]
                    // [ when hasOptional
                    // if _$index == nil {
                    //     return $defaultValue [, nil]
                    // }
                    // ]
                    stringBuilder.append(currentIndex)
                    if (fragment.hasError) {
                        stringBuilder.append(", err")
                    }
                    stringBuilder.append(" := ").append(statement).append("\n")
                    if (fragment.hasError && fragment.hasOptional) {
                        stringBuilder.append("""
                            if err != nil || ${currentIndex} == nil {
                                return ${defaultValue}, err
                            }
                        """.trimIndent()).append("\n")
                    } else if (fragment.hasError) {
                        stringBuilder.append("""
                            if err != nil {
                                return ${defaultValue}, err
                            }
                        """.trimIndent()).append("\n")
                    } else if (fragment.hasOptional) {
                        stringBuilder.append("if ${currentIndex} == nil {\n")
                        if (hasError) {
                            stringBuilder.append("\treturn ${defaultValue}, nil\n")
                        } else {
                            stringBuilder.append("\treturn ${defaultValue}\n")
                        }
                        stringBuilder.append("}\n")
                    }
                }
            }
            return stringBuilder.toString()
        }
    }
}