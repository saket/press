package me.saket.press.shared.theme

import com.badoo.reaktive.rxjavainterop.asRxJava2Observable

fun AppTheme.listenRx() = listen().asRxJava2Observable()
