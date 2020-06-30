package press

import press.di.AppComponent
import press.di.DaggerAppComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("unused")
class DebugPressApp : PressApp() {

  override fun onCreate() {
    super.onCreate()
    Timber.plant(DebugTree())

//    SoLoader.init(this, false)
//    if (FlipperUtils.shouldEnableFlipper(this)) {
//      val client = AndroidFlipperClient.getInstance(this)
//      client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
//      client.addPlugin(DatabasesFlipperPlugin(this))
//      client.start()
//    }
  }

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder().build()
}
