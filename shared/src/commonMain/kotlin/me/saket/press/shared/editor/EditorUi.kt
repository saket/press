package me.saket.press.shared.editor

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.ui.ScreenKey
import me.saket.wysiwyg.formatting.TextSelection

@AndroidParcelize
data class EditorScreenKey(val openMode: EditorOpenMode) : ScreenKey

// Specs for client apps to implement:
//
// - Send all EditorEvents.
//
// - Capitalize the first letter of a heading. See `CapitalizeOnHeadingStart`.
//
// - When a link is clicked, show a menu for letting the user actually open
//   the Url or place the cursor on the Url for editing it.
//
// - Remove rich text formatting when pasting text. See `WysiwygEditText`.
//   On Android, copying a URL will implicitly also copy its underline
//   span, which we don't want.
//
// - Call `AutoFormatOnEnterPress` when enter key is pressed for formatting markdown.
//
// - Render menu items.
interface EditorEvent {
  data class NoteTextChanged(val text: String) : EditorEvent
  data class ArchiveToggleClicked(val archive: Boolean) : EditorEvent
  data class ShareAsClicked(val format: TextFormat) : EditorEvent
  data class CopyAsClicked(val format: TextFormat) : EditorEvent
  object ChangeFolderClicked: EditorEvent
  object DuplicateNoteClicked : EditorEvent
  object SplitScreenClicked : EditorEvent
  object DeleteNoteClicked : EditorEvent

  /** Navigate back to parent from a [ToolbarSubMenu]. */
  object CloseSubMenu : EditorEvent
}

data class EditorModel(
  val hintText: String?,
  val toolbarMenu: List<ToolbarMenuItem>
)

sealed class EditorEffect {
  data class PopulateNoteBody(
    val newText: String,
    val newSelection: TextSelection?
  ) : EditorEffect()

  data class ShowToast(
    val message: String
  ) : EditorEffect()

  object BlockedDueToSyncConflict : EditorEffect()
}

sealed class ToolbarMenuItem {
  abstract val label: String
  abstract val icon: ToolbarIconKind?
  abstract val isDangerousAction: Boolean
}

data class ToolbarMenuAction(
  override val label: String,
  override val isDangerousAction: Boolean = false,
  override val icon: ToolbarIconKind? = null,
  val clickEvent: EditorEvent,
) : ToolbarMenuItem()

data class ToolbarSubMenu(
  override val label: String,
  val subMenuTitle: String = label,
  override val icon: ToolbarIconKind? = null,
  val children: List<ToolbarMenuItem>
) : ToolbarMenuItem() {
  override val isDangerousAction = false
}

enum class ToolbarIconKind {
  MoveToFolder,
  Archive,
  Unarchive,
  ShareAs,
  CopyAs,
  DuplicateNote,
  OpenInSplitScreen,
  DeleteNote
}
