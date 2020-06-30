package me.saket.kgit

object SshKeygen

/**
 * Exposed as an extension function because KMP doesn't allow named arguments
 * for members of expected classes. Star:
 * [https://youtrack.jetbrains.com/issue/KT-39952]
 *
 * @param comment text to add at the end of the public key for identifying
 * the creator. People usually use their email address here.
 */
expect fun SshKeygen.generateRsa(comment: String): SshKeyPair
