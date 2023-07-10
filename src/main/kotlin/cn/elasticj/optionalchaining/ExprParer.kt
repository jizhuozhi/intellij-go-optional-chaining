package cn.elasticj.optionalchaining

import java.util.stream.Collectors.toList

class ExprParer {

    data class ExprNode(val fragments: List<FragmentNode>)

    data class FragmentNode(val statement: String, val hasOptional: Boolean, val hasError: Boolean)

    companion object {
        fun parse(expr: String): ExprNode {
            val fragments = expr.split(".").stream().map {
                if (it.endsWith("?!")) {
                    FragmentNode(it.substring(0, it.length - 2), hasOptional = true, hasError = true)
                } else if (it.endsWith("?")) {
                    FragmentNode(it.substring(0, it.length - 1), hasOptional = true, hasError = false)
                } else if (it.endsWith("!")) {
                    FragmentNode(it.substring(0, it.length - 1), hasOptional = false, hasError = true)
                } else {
                    FragmentNode(it, hasOptional = false, hasError = false)
                }
            }.collect(toList())

            return ExprNode(reduceFragments(fragments))
        }

        private fun reduceFragments(fragments: List<FragmentNode>): List<FragmentNode> {
            val result = mutableListOf<FragmentNode>()
            for (fragment in fragments) {
                if (result.isEmpty()) {
                    result.add(fragment)
                } else {
                    val last = result[result.size - 1]
                    if (!last.hasOptional && !last.hasError) {
                        result[result.size - 1] = FragmentNode("${last.statement}.${fragment.statement}", fragment.hasOptional, fragment.hasError)
                    } else {
                        result.add(fragment)
                    }
                }
            }
            return result
        }
    }
}