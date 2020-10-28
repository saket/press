package me.saket.kgit

import java.lang.reflect.Method

private typealias MethodName = String
private typealias ParamTypes = Array<out Class<out Any>>

inline fun <reified T : Any> reflect(): Reflection =
  Reflection(T::class.java)

class Reflection(private val clazz: Class<out Any>) {
  fun method(name: MethodName, vararg paramTypes: Class<out Any>): Method =
    methodCache.getOrPut(
      key = Triple(clazz, name, paramTypes),
      defaultValue = {
        clazz.getDeclaredMethod(name, *paramTypes)
          .apply { isAccessible = true }
      }
    )

  companion object {
    val methodCache = mutableMapOf<Triple<Class<out Any>, MethodName, ParamTypes>, Method>()
  }
}
