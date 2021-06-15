package me.zhanghai.android.files.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@Throws(ReflectiveOperationException::class)
fun lazyReflectedClass(className: String): Lazy<Class<*>> = lazy { getReflectedClass(className) }

@Throws(ReflectiveOperationException::class)
private fun getReflectedClass(className: String): Class<*> = Class.forName(className)

@Throws(ReflectiveOperationException::class)
fun lazyReflectedField(declaringClass: Class<*>, fieldName: String): Lazy<Field> = lazy {
    getReflectedField(declaringClass, fieldName)
}

@Throws(ReflectiveOperationException::class)
fun lazyReflectedField(declaringClassName: String, fieldName: String): Lazy<Field> = lazy {
    getReflectedField(getReflectedClass(declaringClassName), fieldName)
}

@Throws(ReflectiveOperationException::class)
private fun getReflectedField(declaringClass: Class<*>, fieldName: String) =
    declaringClass.getDeclaredField(fieldName).also { it.isAccessible = true }

@Throws(ReflectiveOperationException::class)
fun <T> lazyReflectedConstructor(
    declaringClass: Class<T>,
    vararg parameterTypes: Any
): Lazy<Constructor<T>> = lazy {
    getReflectedConstructor(declaringClass, *getParameterTypes(parameterTypes))
}

@Throws(ReflectiveOperationException::class)
fun lazyReflectedConstructor(
    declaringClassName: String,
    vararg parameterTypes: Any
): Lazy<Constructor<*>> = lazy {
    getReflectedConstructor(
        getReflectedClass(declaringClassName), *getParameterTypes(parameterTypes)
    )
}

@Throws(ReflectiveOperationException::class)
private fun <T> getReflectedConstructor(declaringClass: Class<T>, vararg parameterTypes: Class<*>) =
    declaringClass.getDeclaredConstructor(*parameterTypes).also { it.isAccessible = true }

@Throws(ReflectiveOperationException::class)
fun lazyReflectedMethod(
    declaringClass: Class<*>,
    methodName: String,
    vararg parameterTypes: Any
): Lazy<Method> = lazy {
    getReflectedMethod(declaringClass, methodName, *getParameterTypes(parameterTypes))
}

@Throws(ReflectiveOperationException::class)
fun lazyReflectedMethod(
    declaringClassName: String,
    methodName: String,
    vararg parameterTypes: Any
): Lazy<Method> = lazy {
    getReflectedMethod(
        getReflectedClass(declaringClassName), methodName, *getParameterTypes(parameterTypes)
    )
}

@Throws(ReflectiveOperationException::class)
private fun getReflectedMethod(
    declaringClass: Class<*>,
    methodName: String,
    vararg parameterTypes: Class<*>
) = declaringClass.getDeclaredMethod(methodName, *parameterTypes).also { it.isAccessible = true }

@Throws(ReflectiveOperationException::class)
private fun getParameterTypes(parameterTypes: Array<out Any>): Array<Class<*>> =
    Array(parameterTypes.size) {
        when (val parameterType = parameterTypes[it]) {
            is Class<*> -> parameterType
            is String -> getReflectedClass(parameterType)
            else -> throw IllegalArgumentException(parameterType.toString())
        }
    }
