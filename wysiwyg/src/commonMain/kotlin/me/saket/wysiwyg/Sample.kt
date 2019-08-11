package me.saket.wysiwyg

expect object Platform {
    val name: String
}

class Greeting {
    fun hello(): String =  "Hello, ${Platform.name}"
}



