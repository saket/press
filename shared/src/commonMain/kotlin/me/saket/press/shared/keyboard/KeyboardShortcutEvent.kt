package me.saket.press.shared.keyboard

@Suppress("DataClassPrivateConstructor")
data class KeyboardShortcutEvent private constructor(val keys: List<Key>) {

  constructor(modifiers: List<Key>, character: String) : this(modifiers + Key.valueOfIgnoreCase(character))
  constructor(vararg keys: Key) : this(keys.toList())

  enum class Key {
    N,
    CMD;

    internal companion object {
      fun valueOfIgnoreCase(character: String): Key {
        // Using valueOf(character.toUppercase()) would be
        // nice, but Kotlin MP doesn't accept a locale.
        return values()
          .firstOrNull { it.name.equals(character, ignoreCase = true) }
          ?: error("Mapping missing for $character")
      }
    }
  }
}
