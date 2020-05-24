package me.saket.kgit

expect object Platform {
  val name: String
}

fun hello(): String = "Hello from ${Platform.name}"
