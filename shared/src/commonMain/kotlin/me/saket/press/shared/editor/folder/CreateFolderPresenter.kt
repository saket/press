package me.saket.press.shared.editor.folder

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.wrap
import me.saket.press.PressDatabase
import me.saket.press.shared.editor.folder.CreateFolderEvent.FolderPathTextChanged
import me.saket.press.shared.editor.folder.CreateFolderEvent.SubmitClicked
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.Invalid
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.InvalidReason.EndsWithSpaceOrDot
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.InvalidReason.InvalidCharacters
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.InvalidReason.InvalidName
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.InvalidReason.TooLong
import me.saket.press.shared.editor.folder.CreateFolderPresenter.NameValidationResult.Valid
import me.saket.press.shared.editor.folder.CreateFolderPresenter.SubmitResult.Failure
import me.saket.press.shared.editor.folder.CreateFolderPresenter.SubmitResult.Idle
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.mapToList
import me.saket.press.shared.syncer.git.FolderPaths
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.ui.highlight

// TODO: tests.
@Suppress("NAME_SHADOWING")
class CreateFolderPresenter(
  private val args: Args,
  private val database: PressDatabase,
  private val schedulers: Schedulers,
  private val strings: Strings,
) : Presenter<CreateFolderEvent, CreateFolderModel>() {
  private val folderPaths = FolderPaths(database)

  override fun models(): ObservableWrapper<CreateFolderModel> {
    return viewEvents().publish { events ->
      val pathChanges = events
        .ofType<FolderPathTextChanged>()
        .map { it.path.removeSuffix("/") }

      pathChanges.publish { pathChanges ->
        combineLatest(
          folderNameSuggestions(pathChanges),
          handleSubmits(events, pathChanges)
        ) { suggestions, submitResult ->
          CreateFolderModel(
            errorMessage = if (submitResult is Failure) submitResult.message else null,
            suggestions = suggestions
          )
        }
      }
    }.wrap()
  }

  private fun folderNameSuggestions(
    pathChanges: Observable<String>
  ): Observable<List<FolderSuggestionModel>> {
    val allFolderPaths = database.folderQueries.allNonEmptyFolders()
      .asObservable(schedulers.io)
      .mapToList()
      .distinctUntilChanged()
      .map { folders ->
        folders.map {
          folderPaths.createFlatPath(id = it.id, existingFolders = { folders })
        }
      }

    return combineLatest(allFolderPaths, pathChanges) { allPaths, searchText ->
      allPaths
        .filter { it.startsWith(searchText, ignoreCase = true) }
        .map { FolderSuggestionModel(name = it.highlight(searchText)) }
    }
  }

  private fun handleSubmits(
    events: Observable<CreateFolderEvent>,
    pathChanges: Observable<String>
  ): Observable<SubmitResult> {
    val validations = events.ofType<SubmitClicked>()
      .withLatestFrom(pathChanges, ::Pair)
      .observeOn(schedulers.io)
      .map { (_, path) -> validateFolderName(path) }

    return validations.publish { validations ->
      merge(
        validations.ofType<Valid>().consumeOnNext {
          database.noteQueries.updateFolders(
            ids = args.screenKey.includeNoteIds,
            folderId = folderPaths.mkdirs(it.path)
          )
          args.navigator.goBack()
        },
        validations.ofType<Invalid>().map {
          Failure(
            // todo: string resource
            message = when (it.reason) {
              TooLong -> "Exceeds 255 characters"
              EndsWithSpaceOrDot -> "Can't end with a space or dot"
              InvalidCharacters -> "Contains invalid characters"
              InvalidName -> "Invalid name"
            }
          )
        },
        pathChanges.map { Idle }
      )
    }
  }

  private fun validateFolderName(path: String): NameValidationResult {
    // https://stackoverflow.com/a/31976060/2511884
    if (path.length > 255) {
      return Invalid(TooLong)
    }
    if (path.endsWith('.') || path.endsWith(' ')) {
      return Invalid(EndsWithSpaceOrDot)
    }
    val forbiddenChars = listOf(':', '<', '>', '"', '\\', '|', '?', '*', ' ')
    forbiddenChars.forEach {
      if (it in path) {
        return Invalid(InvalidCharacters)
      }
    }

    val forbiddenNames = listOf(
      "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1",
      "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
    )
    val pathParts = path.split('/')
    forbiddenNames.forEach { name ->
      pathParts.forEach { part ->
        if (name == part) {
          return Invalid(InvalidName)
        }
      }
    }
    return Valid(path)
  }

  private sealed class NameValidationResult {
    data class Valid(val path: String) : NameValidationResult()
    data class Invalid(val reason: InvalidReason) : NameValidationResult()

    enum class InvalidReason {
      TooLong,
      EndsWithSpaceOrDot,
      InvalidCharacters,
      InvalidName,
    }
  }

  private sealed class SubmitResult {
    object Idle : SubmitResult()
    data class Failure(val message: String) : SubmitResult()
  }

  fun interface Factory {
    fun create(args: Args): CreateFolderPresenter
  }

  data class Args(
    val screenKey: CreateFolderScreenKey,
    val navigator: Navigator,
  )
}
