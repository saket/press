package press.sync

import android.content.Context
import android.util.AttributeSet
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.press.R
import me.saket.press.shared.sync.git.NewGitRepositoryPresenter
import press.widgets.PressDialogView

class NewGitRepositoryView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: NewGitRepositoryPresenter.Factory
) : ContourLayout(context) {

  private val dialogView = PressDialogView(context)

  init {
    id = R.id.newgitrepo_view
    dialogView.layoutBy(
      x = centerHorizontallyTo { parent.centerX() },
      y = centerVerticallyTo { parent.centerY() }
    )
  }
}
