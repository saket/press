package me.saket.kgit

import org.eclipse.jgit.revwalk.RevCommit
import java.time.Instant
import java.time.ZoneId

actual class GitCommit(internal val commit: RevCommit) {
  actual val sha1: GitSha1
    get() = GitSha1(commit.id)

  actual val message: String
    get() = commit.fullMessage

  actual val author: GitAuthor
    get() = GitAuthor(name = commit.authorIdent.name, email = commit.authorIdent.emailAddress)

  actual val timestamp: UtcTimestamp
    get() = UtcTimestamp(
        Instant.ofEpochMilli(commit.authorIdent.`when`.time)
            .atZone(ZoneId.of(commit.authorIdent.timeZone.id))
            .toInstant()
            .toEpochMilli()
    )
}
