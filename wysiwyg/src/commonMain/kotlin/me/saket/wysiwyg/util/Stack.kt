package me.saket.wysiwyg.util

expect class Stack<E>() {
  fun isEmpty(): Boolean
  fun pop(): E
  fun add(element: E): Boolean
}
