package me.saket.kgit

data class MergeConflict(
  val path: String,
  val theirContent: () -> TheirContent
) {

  sealed class TheirContent {
    data class Modified(val content: String) : TheirContent() {
      override fun toString() = "Modified(content=${content.replace("\n", " ")})"
    }
    object Deleted : TheirContent()
  }
}
