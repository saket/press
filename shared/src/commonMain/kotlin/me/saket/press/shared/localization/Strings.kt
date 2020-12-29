package me.saket.press.shared.localization

import me.saket.press.shared.localization.Strings.Common
import me.saket.press.shared.localization.Strings.Editor
import me.saket.press.shared.localization.Strings.Home
import me.saket.press.shared.localization.Strings.Sync

data class Strings(
  val common: Common,
  val home: Home,
  val editor: Editor,
  val sync: Sync
) {
  data class Common(
    val app_name: String,
    val close_nav_icon_description: String,
    val generic_error: String,
    val retry: String,
    val share_picker_title: String
  )

  data class Home(
    val preferences: String
  )

  data class Editor(
    val new_note_hints: List<String>,
    val open_url: String,
    val edit_url: String,

    val menu_archive: String,
    val menu_unarchive: String,
    val menu_share_as: String,
    val menu_share_as_markdown: String,
    val menu_share_as_html: String,
    val menu_share_as_richtext: String,
    val menu_copy_as: String,
    val menu_copy_as_markdown: String,
    val menu_copy_as_html: String,
    val menu_copy_as_richtext: String,
    val menu_duplicate_note: String,

    val note_archived: String,
    val note_unarchived: String,
    val note_copied: String
  )

  data class Sync(
    val title: String,
    val confirm_repo_message: String,
    val confirm_repo_confirm_button: String,
    val confirm_repo_cancel_button: String,
    val setup_sync_with_host: String,
    val open_repository: String,
    val remove_repository: String,
    val remove_repository_confirm_question: String,
    val remove_repository_confirm: String,
    val remove_repository_cancel: String,
    val show_sync_stats: String,
    val sync_disabled_message: String,
    val cd_sync_repository_options: String,
    val status_in_flight: String,
    val status_failed: String,
    val status_idle_never_synced: String,
    val status_synced_x_ago: String,
    val timestamp_now: String,
    val timestamp_minutes: String,
    val timestamp_hours: String,
    val timestamp_days: String,
    val timestamp_a_while_ago: String,
    val search_git_repos: String,

    val conflicted_note_heading_prefix: String,
    val conflicted_note_explanation_dialog_title: String,
    val conflicted_note_explanation_dialog_message: String,
    val conflicted_note_explanation_dialog_button: String,

    val nerd_stats_title: String,
    val nerd_stats_git_size: String,
    val nerd_stats_logs_label: String,

    val newgitrepo_title: String,
    val newgitrepo_name_hint: String,
    val newgitrepo_submit: String,
    val newgitrepo_cancel: String,
  )
}

val ENGLISH_STRINGS = Strings(
  common = Common(
    app_name = "Press",
    close_nav_icon_description = "Go back",
    generic_error = "Something went wrong, try again?",
    retry = "Retry",
    share_picker_title = "Share with"
  ),
  home = Home(
    preferences = "Preferences"
  ),
  editor = Editor(
    new_note_hints = listOf(
      "A wonderful note",
      "It begins with a word",
      "This is the beginning",
      "Once upon a time",
      "Unleash those wild ideas",
      "Untitled composition",
      "Here we go",
      "Type your heart out"
    ),
    open_url = "Open",
    edit_url = "Edit",

    menu_archive = "Archive",
    menu_unarchive = "Unarchive",
    menu_share_as = "Share as",
    menu_share_as_markdown = "Markdown",
    menu_share_as_html = "HTML",
    menu_share_as_richtext = "Rich Text",
    menu_copy_as = "Copy as",
    menu_copy_as_markdown = "Markdown",
    menu_copy_as_html = "HTML",
    menu_copy_as_richtext = "Rich Text",
    menu_duplicate_note = "Duplicate note",

    note_archived = "Note archived",
    note_unarchived = "Note unarchived",
    note_copied = "Ready to be pasted"
  ),
  sync = Sync(
    title = "Sync",
    confirm_repo_message = "Are you sure you want to give Press access to <b>%s</b>?",
    confirm_repo_confirm_button = "Let's go",
    confirm_repo_cancel_button = "Wait no",
    setup_sync_with_host = "Sync with %s",
    sync_disabled_message = "Press can sync notes between your devices through a git repository. This is a " +
      "work-in-progress feature and may cause paranormal events around you.",
    open_repository = "Open",
    remove_repository = "Remove",
    remove_repository_confirm_question = "Are you sure?",
    remove_repository_confirm = "Yep",
    remove_repository_cancel = "Cancel",
    show_sync_stats = "Stats for nerds",
    status_in_flight = "Syncing…",
    status_idle_never_synced = "Waiting to sync",
    status_failed = "Last attempt failed, will retry in sometime.",
    status_synced_x_ago = "Synced %s",
    timestamp_now = "just now",
    timestamp_minutes = "%sm ago",
    timestamp_hours = "%sh ago",
    timestamp_days = "%sd ago",
    timestamp_a_while_ago = "a while ago",
    cd_sync_repository_options = "Show options for %s git repository",
    search_git_repos = "Search your repositories…",

    conflicted_note_heading_prefix = "Conflicted",
    conflicted_note_explanation_dialog_title = "Sync conflict detected",
    conflicted_note_explanation_dialog_message = "This note was edited on another device in a conflicting way, " +
      "and had to be duplicated. Please close and re-open this note? \n\nSync conflicts are unfortunate but " +
      "unavoidable if edits to the same note are made on multiple devices, either around the same time or at" +
      " different times while offline.",
    conflicted_note_explanation_dialog_button = "Close note",

    nerd_stats_title = "Stats for nerds",
    nerd_stats_git_size = "Git directory size: %s",
    nerd_stats_logs_label = "Logs: ",

    newgitrepo_title = "New private repository",
    newgitrepo_name_hint = "Enter a name…",
    newgitrepo_submit = "Create",
    newgitrepo_cancel = "Cancel"
  )
)
