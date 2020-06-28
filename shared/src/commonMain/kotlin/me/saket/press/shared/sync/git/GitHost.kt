package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.single.Single

interface GitHost {
  fun generateAuthUrl(): String
  fun completeAuth(callbackUrl: String): Single<Authorized>

  interface Authorized {
    fun addDeployKey(repositoryName: String, sshPublicKey: String): Completable
  }
}

