package me.saket.wysiwyg

expect object Platform {
    val name: String
}

expect class SingleThreadBackgroundExecutor() {
    fun <R> enqueue(runnable: () -> R)
}

expect object UiThreadExecutor {
    fun enqueue(runnable: () -> Unit)
}
