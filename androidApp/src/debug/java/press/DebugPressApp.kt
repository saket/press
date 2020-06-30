package press

import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import press.di.AppComponent
import press.di.DaggerAppComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("unused")
class DebugPressApp : PressApp() {

  override fun onCreate() {
    super.onCreate()
    Timber.plant(DebugTree())

    SoLoader.init(this, false)
    if (FlipperUtils.shouldEnableFlipper(this)) {
      val client = AndroidFlipperClient.getInstance(this)
      client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
      client.addPlugin(DatabasesFlipperPlugin(this))
      client.addPlugin(SharedPreferencesFlipperPlugin(this))
      client.start()
    }
  }

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder().build()
}
