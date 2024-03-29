package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import java.io.ByteArrayOutputStream

actual class RealSshKeygen : SshKeygen {
  override fun generateEcdsa(comment: String): SshKeyPair {
    val keyPair: KeyPair = KeyPair.genKeyPair(JSch(), KeyPair.ECDSA, 521)

    var publicKey: String
    ByteArrayOutputStream().use { stream ->
      keyPair.writePublicKey(stream, comment)
      publicKey = stream.toString()
    }

    var privateKey: String
    ByteArrayOutputStream().use { stream ->
      keyPair.writePrivateKey(stream)
      privateKey = stream.toString()
    }

    keyPair.dispose()
    return SshKeyPair(publicKey, SshPrivateKey(privateKey))
  }
}
