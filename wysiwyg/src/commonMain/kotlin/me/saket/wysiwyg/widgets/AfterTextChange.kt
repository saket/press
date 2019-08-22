package me.saket.wysiwyg.widgets

class AfterTextChange(
  val callback: AfterTextChange.(text: EditableText) -> Unit
) {
  var isAvoidingInfiniteLoop: Boolean = false

  fun suspendTextChangesAndRun(block: () -> Unit) {
    isAvoidingInfiniteLoop = true
    block()
    isAvoidingInfiniteLoop = false
  }
}