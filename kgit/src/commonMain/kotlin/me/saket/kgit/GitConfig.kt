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

fun GitConfig.author(): GitIdentity {
  return this["author"].let { GitIdentity(it["name"], it["email"]) }
}

fun GitConfig.committer(): GitIdentity {
  return this["committer"].let { GitIdentity(it["name"], it["email"]) }
}

private operator fun GitConfig.get(section: String): Section {
  return sections.single { it.name == section }
}

private operator fun Section.get(key: String): String {
  for ((k, v) in values) {
    if (k == key) {
      return v
    }
  }
  error("$key doesn't exist in $values")
}
