package me.saket.press.shared.sync

import com.badoo.reaktive.test.completable.test
import io.ktor.client.HttpClient
import me.saket.press.shared.sync.github.GithubSyncer
import kotlin.test.Test

class GithubSyncerTest {

  private val syncer = GithubSyncer(HttpClient())

  @Test fun auth() {
    println("Starting user auth")
    syncer.startUserAuth().test()
  }
}
