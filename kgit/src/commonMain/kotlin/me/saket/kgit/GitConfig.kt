package me.saket.kgit

import me.saket.kgit.GitConfig.Section

data class GitConfig(val sections: List<Section>) {
  data class Section(val name: String, val values: List<Pair<String, String>>)
}

/**
 * Example usage:
 *
 * ```
 * GitConfig("diff" to listOf("renames" to "true"))
 * ```
 */
@Suppress("FunctionName")
fun GitConfig(vararg sections: Pair<String, List<Pair<String, String>>>) =
  GitConfig(sections.map { (name, values) -> Section(name, values) })
