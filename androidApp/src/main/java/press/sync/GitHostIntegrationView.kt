package press.sync

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter.Args
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.navigator
import press.theme.themeAware
import press.theme.themed
import press.widgets.PressToolbar

class GitHostIntegrationView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted deepLink: String,
  @Assisted private val onDismiss: () -> Unit,
  presenterFactory: GitHostIntegrationPresenter.Factory
) : ContourLayout(context) {

  private val presenter = presenterFactory.create(Args(
      deepLink = deepLink,
      navigator = navigator()
  ))

  private val toolbar = themed(PressToolbar(context)).apply {
    title = GitHost.readHostFromDeepLink(deepLink).displayName()
    setNavigationOnClickListener { onDismiss() }
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val repoAdapter = GitRepositoryAdapter()
  private val recyclerView = themed(RecyclerView(context)).apply {
    layoutManager = LinearLayoutManager(context)
    adapter = repoAdapter
    applyLayout(
        x = matchParentX(),
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val progressView = themed(ProgressBar(context)).apply {
    isGone = true
    isIndeterminate = true
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() }.widthOf { 60.xdip },
        y = centerVerticallyTo { parent.centerY() }.heightOf { 60.ydip }
    )
  }

  private val errorView = ErrorView(context).also {
    it.isGone = true
    it.applyLayout(
        x = matchParentX(),
        y = centerVerticallyTo { parent.centerY() }
    )
  }

  init {
    themeAware {
      background = ColorDrawable(it.window.backgroundColor)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    repoAdapter.onClick = {
      ConfirmRepoSelectionDialog.show(context, repo = it, onConfirm = {
        presenter.dispatch(GitRepositoryClicked(it))
      })
    }

    presenter.uiUpdates()
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(models = ::render)
  }

  private fun render(model: GitHostIntegrationUiModel) {
    progressView.isGone = model !is ShowProgress
    errorView.isGone = model !is ShowFailure
    recyclerView.isGone = model !is SelectRepo

    return when (model) {
      is ShowProgress -> Unit
      is ShowFailure -> {
        errorView.retryButton.setOnClickListener {
          presenter.dispatch(RetryClicked(model.kind))
        }
      }
      is SelectRepo -> repoAdapter.submitList(model.repositories)
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(
      context: Context,
      deepLink: String,
      onDismiss: () -> Unit
    ): GitHostIntegrationView
  }
}
