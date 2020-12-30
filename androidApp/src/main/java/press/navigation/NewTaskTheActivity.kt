package press.navigation

import android.content.Intent

/**
 * The sole reason this empty sub-class exists is to allow duplication of [TheActivity] in split screen
 * using [Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT], which doesn't seem to work if [TheActivity]'s launchMode
 * is set to singleTop.
 */
class NewTaskTheActivity : TheActivity()
