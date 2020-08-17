package me.saket.kgit

data class MergeConflict(
  val path: String,
  val theirContent: () -> TheirContent
) {

  sealed class TheirContent {
    data class ModifiedOnRemote(val content: String) : TheirContent() {
      override fun toString() = "ModifiedOnRemote(content=${content.replace("\n", " ")})"
    }

    object DeletedOnRemote : TheirContent() {
      override fun toString() = "DeletedOnRemote"
    }
  }
}
