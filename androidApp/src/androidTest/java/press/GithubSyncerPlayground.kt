package press

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import me.saket.press.shared.network.SharedNetworkComponent
import me.saket.press.shared.sync.github.GithubSyncer
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GithubSyncerPlayground {

  private val syncer: GithubSyncer
    get() = SharedNetworkComponent.syncer() as GithubSyncer

  @Test fun auth() {
    println("Starting user auth")
    syncer.startUserAuth()
        .asRxJava2Completable()
        .blockingAwait()
  }
}
