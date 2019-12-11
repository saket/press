package press.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

typealias FieldName = String
typealias MethodName = String

fun reflect(klass: KClass<out Any>, fieldName: FieldName): Field =
  Reflection(klass.java).field(fieldName)

inline fun <reified T : Any> reflect(): Reflection =
  Reflection(T::class.java)

class Reflection(private val clazz: Class<out Any>) {
  fun field(name: FieldName): Field =
    fieldCache.getOrPut(
        key = clazz to name,
        defaultValue = { clazz.getDeclaredField(name).apply { isAccessible = true } }
    )

  fun method(name: MethodName, vararg paramTypes: Class<out Any>): Method =
    methodCache.getOrPut(
        key = Triple(clazz, name, paramTypes),
        defaultValue = {
          clazz.getDeclaredMethod(name, *paramTypes)
              .apply { isAccessible = true }
        }
    )

  companion object {
    val fieldCache = mutableMapOf<Pair<Class<out Any>, FieldName>, Field>()
    val methodCache = mutableMapOf<Triple<Class<out Any>, MethodName, Array<out Class<out Any>>>, Method>()
  }
}
