package compose.util

import java.lang.reflect.Field
import kotlin.reflect.KClass

typealias FieldName = String

fun reflect(klass: KClass<out Any>, fieldName: FieldName) = Reflection(klass.java).field(fieldName)

class Reflection(private val clazz: Class<out Any>) {
  fun field(name: String): Field =
    fieldCache.getOrPut(
        key = clazz to name,
        defaultValue = { clazz.getDeclaredField(name).apply { isAccessible = true } }
    )

  companion object {
    val fieldCache = mutableMapOf<Pair<Class<out Any>, FieldName>, Field>()
  }
}