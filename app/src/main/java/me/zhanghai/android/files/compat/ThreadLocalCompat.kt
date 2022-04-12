package me.zhanghai.android.files.compat

import android.os.Build
import kotlin.reflect.KClass

fun <T> KClass<ThreadLocal<*>>.withInitial(supplier: () -> T): ThreadLocal<T> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ThreadLocal.withInitial(supplier)
    } else {
        object : ThreadLocal<T>() {
            override fun initialValue(): T = supplier.invoke()
        }
    }
