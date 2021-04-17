package me.saket.press.shared.localization

import me.saket.press.shared.localization.Strings.Common
import me.saket.press.shared.localization.Strings.Editor
import me.saket.press.shared.localization.Strings.Home
import me.saket.press.shared.localization.Strings.Preferences
import me.saket.press.shared.localization.Strings.Sync

class Strings(
  val common: Common,
  val home: Home,
  val editor: Editor,
  val sync: Sync,
  val prefs: Preferences,
) {
  class Common(
    val app_name: String,
    val close_nav_icon_contentdescription: String,
    val generic_error: String,
    val retry: String,
    val share_picker_title: String
  )

  class Home(
    val menu_preferences: String,
    val menu_search_notes: String,
    val close_search_contentdescriptoin: String,
    val searchnotes_everywhere_hint: String,
    val searchnotes_in_folder_hint: String,
  )

  class Editor(
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
    val menu_open_in_split_screen: String,
    val menu_delete_note: String,
    val menu_delete_note_confirmation_title: String,
    val menu_delete_note_confirm: String,
    val menu_delete_note_cancel: String,

    val note_archived: String,
    val note_unarchived: String,
    val note_copied: String,

    val formattingtoolbar_undo: String,
    val formattingtoolbar_redo: String,
    val formattingtoolbar_strong_emphasis: String,
    val formattingtoolbar_emphasis: String,
    val formattingtoolbar_strikethrough: String,
    val formattingtoolbar_heading: String,
    val formattingtoolbar_blockquote: String,
    val formattingtoolbar_inline_code: String,
  )

  class Sync(
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
    val timestamp_a_while_ago: String,
    val search_git_repos: String,

    val conflicted_note_heading_prefix: String,
    val conflicted_note_explanation_dialog_title: String,
    val conflicted_note_explanation_dialog_message: String,
    val conflicted_note_explanation_dialog_button: String,

    val nerd_stats_title: String,
    val nerd_stats_git_size: String,
    val nerd_stats_git_size_unavailable: String,
    val nerd_stats_logs_label: String,
    val nerd_stats_emptystate: String,

    val newgitrepo_title: String,
    val newgitrepo_name_hint: String,
    val newgitrepo_submit: String,
    val newgitrepo_cancel: String,
  )

  class Preferences(
    val screen_title: String,
    val category_title_editor: String,
    val category_subtitle_editor: String,
    val category_title_theme: String,
    val category_subtitle_theme: String,
    val category_title_sync: String,
    val category_subtitle_sync: String,
    val category_title_about_app: String,
    val category_subtitle_about_app: String,

    val editor_preview_title: String,
    val editor_typeface: String,
    val editor_autocorrect: String,

    val theme_themeMode_title: String,
    val theme_themeMode_always_dark: String,
    val theme_themeMode_never_dark: String,
    val theme_themeMode_match_system: String,

    val about_playstore_link_title: String,
    val about_header_html: String,
    val about_github_link_title: String,
    val about_credits_title: String,
  )
}

val ENGLISH_STRINGS = Strings(
  common = Common(
    app_name = "Press",
    close_nav_icon_contentdescription = "Go back",
    generic_error = "Something went wrong, try again?",
    retry = "Retry",
    share_picker_title = "Share with"
  ),
  home = Home(
    menu_preferences = "Preferences",
    menu_search_notes = "Search notes",
    searchnotes_everywhere_hint = "Search notes…",
    searchnotes_in_folder_hint = "Search in %s…",
    close_search_contentdescriptoin = "Close search",
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
    menu_open_in_split_screen = "Split screen",
    menu_delete_note = "Delete note",
    menu_delete_note_confirmation_title = "Are you sure?",
    menu_delete_note_confirm = "Confirm delete",
    menu_delete_note_cancel = "Wait no",

    note_archived = "Note archived",
    note_unarchived = "Note unarchived",
    note_copied = "Ready to be pasted",

    formattingtoolbar_undo = "Undo",
    formattingtoolbar_redo = "Redo",
    formattingtoolbar_strong_emphasis = "Bold",
    formattingtoolbar_emphasis = "Italic",
    formattingtoolbar_strikethrough = "Strikethrough",
    formattingtoolbar_heading = "Heading",
    formattingtoolbar_blockquote = "Quote",
    formattingtoolbar_inline_code = "Code"
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
    nerd_stats_git_size_unavailable = "N / A",
    nerd_stats_logs_label = "Logs: ",
    nerd_stats_emptystate = "Logs will be shown here after the first sync",

    newgitrepo_title = "New private repository",
    newgitrepo_name_hint = "Enter a name…",
    newgitrepo_submit = "Create",
    newgitrepo_cancel = "Cancel"
  ),
  prefs = Preferences(
    screen_title = "Preferences",
    category_title_editor = "Editor",
    category_subtitle_editor = "Typography and spacings",
    category_title_theme = "Theme",
    category_subtitle_theme = "Colors and dark mode",
    category_title_sync = "Sync",
    category_subtitle_sync = "Your notes, available anywhere through git",
    category_title_about_app = "About Press",
    category_subtitle_about_app = "Send <strike>complaints</strike> kudos here",

    editor_preview_title = "Preview",
    editor_typeface = "Typeface",
    editor_autocorrect = "Auto-correct typos",

    theme_themeMode_title = "Theme mode",
    theme_themeMode_always_dark = "Always dark",
    theme_themeMode_never_dark = "Always light",
    theme_themeMode_match_system = "Match system",

    about_header_html = "Press is maintained by <a href=\"https://saket.me\">Saket Narayan</a>. It was created" +
      " as a result of their frustration for lack of a minimal & cross-platform markdown note taking app for " +
      "Android. Follow them <a href=\"https://twitter.com/saketme\">@saketme</a> on twitter for progress updates!",
    about_playstore_link_title = "Rate on Play Store ❤️",
    about_github_link_title = "View source on GitHub",
    about_credits_title = "Licenses & Attributes",
  )
)
