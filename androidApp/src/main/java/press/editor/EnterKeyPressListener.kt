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
        private var enterPressed: Boolean = false
        private var cursorBeforeEnter = -1

        var isAvoidingInfiniteLoop: Boolean = false

        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
          cursorBeforeEnter = Selection.getSelectionStart(text)
        }

        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
          enterPressed = count == 1 && text[start] == '\n'
        }

        override fun afterTextChanged(text: Editable) {
          if (enterPressed && isAvoidingInfiniteLoop.not()) {
            isAvoidingInfiniteLoop = true
            emitter.onNext(EnterKeyPressed(selectionBeforeEnter = TextSelection.cursor(cursorBeforeEnter)))
            isAvoidingInfiniteLoop = false
          }
        }
      }
      emitter.setCancellable { view.removeTextChangedListener(textWatcher) }
      view.addTextChangedListener(textWatcher)
    }
}
