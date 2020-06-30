package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import kotlinx.io.ByteArrayOutputStream

/**
 * @param comment text to add at the end of the public key for identifying
 * the creator. People usually use their email address here.
 */
actual fun SshKeygen.generateRsa(comment: String): SshKeyPair {
  val keyPair: KeyPair = KeyPair.genKeyPair(JSch(), KeyPair.RSA)

  var publicKey = ""
  ByteArrayOutputStream().use { stream ->
    keyPair.writePublicKey(stream, comment)
    publicKey = stream.toString()
  }

  var privateKey = ""
  ByteArrayOutputStream().use { stream ->
    keyPair.writePrivateKey(stream)
    privateKey = stream.toString()
  }

  keyPair.dispose()
  return SshKeyPair(publicKey, SshPrivateKey(privateKey))
}
