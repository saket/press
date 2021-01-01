package me.saket.press.shared.syncer.git

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import me.saket.kgit.SshKeyPair
import me.saket.kgit.SshKeygen
import me.saket.kgit.SshPrivateKey

class FakeSshKeygen : SshKeygen {
  val key = AtomicReference(SshKeyPair(publicKey = "", privateKey = SshPrivateKey("")))
  override fun generateRsa(comment: String) = key.value
}
