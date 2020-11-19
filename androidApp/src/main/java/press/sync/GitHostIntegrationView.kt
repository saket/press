package press.sync

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.SearchTextChanged
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter.Args
import me.saket.press.shared.sync.git.GitHostIntegrationScreenKey
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.extensions.doOnTextChange
import press.extensions.findParentOfType
import press.extensions.hideKeyboard
import press.extensions.interceptPullToCollapseOnView
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.themeAware
import press.widgets.PressToolbar
import press.widgets.SlideDownItemAnimator
import kotlin.math.abs

class GitHostIntegrationView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: GitHostIntegrationPresenter.Factory
) : ContourLayout(context) {
  private val deepLink = screenKey<GitHostIntegrationScreenKey>().deepLink

  private val presenter = presenterFactory.create(
    Args(
      deepLink = deepLink,
      navigator = navigator()
    )
  )

  private val toolbar = PressToolbar(context).apply {
    title = GitHost.readHostFromDeepLink(deepLink).displayName()
    applyLayout(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
  }

  private val searchView = SearchView(context).apply {
    id = R.id.git_repos_search
    hint = context.strings().sync.search_git_repos
    isGone = true
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { toolbar.bottom() }
    )
  }

  private val repoAdapter = GitRepositoryAdapter()
  private val recyclerView = RecyclerView(context).apply {
    layoutManager = LinearLayoutManager(context)
    adapter = repoAdapter
    itemAnimator = SlideDownItemAnimator().apply { supportsChangeAnimations = false }
    applyLayout(
      x = matchParentX(),
      y = topTo { searchView.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val progressView = ProgressBar(context).apply {
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
    id = R.id.githostintegration_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    recyclerView.addOnScrollListener(object : OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (abs(dy) > 0) hideKeyboard()
      }
    })
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    repoAdapter.onClick = {
      ConfirmRepoSelectionDialog.show(
        context, repo = it,
        onConfirm = {
          hideKeyboard()
          presenter.dispatch(GitRepositoryClicked(it))
        }
      )
    }
    searchView.editText!!.doOnTextChange {
      presenter.dispatch(SearchTextChanged(it.toString()))
    }

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(models = ::render)

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(recyclerView)
  }

  private fun render(model: GitHostIntegrationUiModel) {
    progressView.isGone = model !is ShowProgress
    errorView.isGone = model !is ShowFailure
    recyclerView.isGone = model !is SelectRepo
    searchView.isGone = model !is SelectRepo

    return when (model) {
      is ShowProgress -> Unit
      is ShowFailure -> {
        errorView.retryButton.setOnClickListener {
          presenter.dispatch(RetryClicked(model.kind))
        }
      }
      is SelectRepo -> {
        repoAdapter.submitList(model.repositories) {
          recyclerView.scrollToPosition(0)
        }
      }
    }
  }
}
