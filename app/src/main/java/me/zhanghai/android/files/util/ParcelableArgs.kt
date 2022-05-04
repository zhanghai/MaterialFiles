/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface ParcelableArgs : Parcelable

fun <Args : ParcelableArgs> Bundle.putArgs(args: Args, argsClass: KClass<Args>): Bundle {
    putParcelable(argsClass.java.name, args)
    return this
}

inline fun <reified Args : ParcelableArgs> Bundle.putArgs(args: Args) = putArgs(args, Args::class)

fun <Args : ParcelableArgs> Args.toBundle(argsClass: KClass<Args>): Bundle =
    Bundle().apply { putArgs(this@toBundle, argsClass) }

inline fun <reified Args : ParcelableArgs> Args.toBundle(): Bundle = toBundle(Args::class)

fun <F : Fragment, Args : ParcelableArgs> F.putArgs(args: Args, argsClass: KClass<Args>): F {
    val arguments = arguments
    if (arguments != null) {
        arguments.putArgs(args, argsClass)
    } else {
        this.arguments = args.toBundle(argsClass)
    }
    return this
}

inline fun <F : Fragment, reified Args : ParcelableArgs> F.putArgs(args: Args): F =
    putArgs(args, Args::class)

fun <Args : ParcelableArgs> Intent.putArgs(args: Args, argsClass: KClass<Args>): Intent =
    putExtra(argsClass.java.name, args)

inline fun <reified Args : ParcelableArgs> Intent.putArgs(args: Args) = putArgs(args, Args::class)

fun <Args : ParcelableArgs> Bundle.getArgs(argsClass: KClass<Args>): Args =
    getArgsOrNull(argsClass)!!

inline fun <reified Args : ParcelableArgs> Bundle.getArgs() = getArgs(Args::class)

fun <Args : ParcelableArgs> Bundle.getArgsOrNull(argsClass: KClass<Args>): Args? =
    getParcelableSafe(argsClass.java.name)

inline fun <reified Args : ParcelableArgs> Bundle.getArgsOrNull() = getArgsOrNull(Args::class)

@MainThread
inline fun <reified Args : ParcelableArgs> Activity.args() = BundleArgsLazy(Args::class) {
    intent.extras ?: throw IllegalStateException("Activity $this has null intent extras")
}

@MainThread
inline fun <reified Args : ParcelableArgs> Fragment.args() = BundleArgsLazy(Args::class) {
    arguments ?: throw IllegalStateException("Fragment $this has null arguments")
}

class BundleArgsLazy<Args : ParcelableArgs>(
    private val argsClass: KClass<Args>,
    private val argumentsProducer: () -> Bundle
) : Lazy<Args> {
    private var cached: Args? = null

    override val value: Args
        get() {
            var args = cached
            if (args == null) {
                args = argumentsProducer().getArgs(argsClass)
                cached = args
            }
            return args
        }

    override fun isInitialized() = cached != null
}
