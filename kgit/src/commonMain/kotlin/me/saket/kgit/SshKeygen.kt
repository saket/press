package me.saket.kgit

interface SshKeygen {
  /**
   * @param comment text to add at the end of the public key for identifying
   * the creator. People usually use their email address here.
   */
  fun generateRsa(comment: String): SshKeyPair
}

expect class RealSshKeygen() : SshKeygen
