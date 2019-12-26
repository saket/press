package press.editor

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.Observable
import me.saket.press.shared.editor.EditorEvent.EnterKeyPressed
import me.saket.wysiwyg.formatting.TextSelection

object EnterKeyPressListener {

  fun listen(view: EditText): Observable<EnterKeyPressed> =
    Observable.create<EnterKeyPressed> { emitter ->
      val textWatcher = object : TextWatcher {
        private var event: EnterKeyPressed? = null
        var isAvoidingInfiniteLoop: Boolean = false

        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, newTextLength: Int) {}

        override fun onTextChanged(text: CharSequence, start: Int, before: Int, newTextLength: Int) {
          val enterPressed = newTextLength == 1 && text[start] == '\n'
          event = if (enterPressed) {
            EnterKeyPressed(
                textAfterEnter = text.toString(),
                cursorAfterEnter = TextSelection.cursor(Selection.getSelectionStart(text))
            )
          } else null
        }

        override fun afterTextChanged(text: Editable) {
          if (event != null && isAvoidingInfiniteLoop.not()) {
            isAvoidingInfiniteLoop = true
            emitter.onNext(event!!)
            isAvoidingInfiniteLoop = false
          }
        }
      }
      emitter.setCancellable { view.removeTextChangedListener(textWatcher) }
      view.addTextChangedListener(textWatcher)
    }
}
