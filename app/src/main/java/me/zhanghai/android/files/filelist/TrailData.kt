/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.provider.archive.archiveFile
import me.zhanghai.android.files.provider.archive.isArchivePath

class TrailData private constructor(
    val trail: List<Path>,
    private val states: MutableList<Parcelable?>,
    val currentIndex: Int
) {
    fun navigateTo(lastState: Parcelable, path: Path): TrailData {
        val newTrail = createTrail(path)
        val newStates = mutableListOf<Parcelable?>()
        val newIndex = newTrail.size - 1
        var isPrefix = true
        for (index in newTrail.indices) {
            if (isPrefix && index < trail.size) {
                if (newTrail[index] == trail[index]) {
                    newStates.add(if (index != currentIndex) states[index] else lastState)
                } else {
                    isPrefix = false
                    newStates.add(null)
                }
            } else {
                newStates.add(null)
            }
        }
        if (isPrefix) {
            for (index in newTrail.size until trail.size) {
                newTrail.add(trail[index])
                newStates.add(if (index != currentIndex) states[index] else lastState)
            }
        }
        return TrailData(newTrail, newStates, newIndex)
    }

    fun navigateUp(): TrailData? {
        if (currentIndex == 0) {
            return null
        }
        val newIndex = currentIndex - 1
        return TrailData(trail, states, newIndex)
    }

    val pendingState: Parcelable?
        get() = states.set(currentIndex, null)

    val currentPath: Path
        get() = trail[currentIndex]

    companion object {
        fun of(path: Path): TrailData {
            val trail: List<Path> = createTrail(path)
            val states = MutableList<Parcelable?>(trail.size) { null }
            val index = trail.size - 1
            return TrailData(trail, states, index)
        }

        private fun createTrail(path: Path): MutableList<Path> {
            var path = path
            val trail = mutableListOf<Path>()
            val archiveFile = if (path.isArchivePath) path.archiveFile else null
            while (true) {
                trail.add(path)
                path = path.parent ?: break
            }
            trail.reverse()
            if (archiveFile != null) {
                val archiveFileParent = archiveFile.parent
                if (archiveFileParent != null) {
                    trail.addAll(0, createTrail(archiveFileParent))
                }
            }
            return trail
        }
    }
}
