/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.audio

import android.os.Bundle
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.format
import me.zhanghai.android.files.file.isAudio
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.isMediaMetadataRetrieverCompatible
import me.zhanghai.android.files.util.viewModels

class FilePropertiesAudioTabFragment : FilePropertiesTabFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesAudioTabViewModel(args.path) } }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.audioInfoLiveData.observe(viewLifecycleOwner) { onAudioInfoChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onAudioInfoChanged(stateful: Stateful<AudioInfo>) {
        bindView(stateful) { audioInfo ->
            if (audioInfo.title != null) {
                addItemView(R.string.file_properties_media_title, audioInfo.title)
            }
            if (audioInfo.artist != null) {
                addItemView(R.string.file_properties_audio_artist, audioInfo.artist)
            }
            if (audioInfo.album != null) {
                addItemView(R.string.file_properties_audio_album, audioInfo.album)
            }
            if (audioInfo.albumArtist != null) {
                addItemView(R.string.file_properties_audio_album_artist, audioInfo.albumArtist)
            }
            if (audioInfo.composer != null) {
                addItemView(R.string.file_properties_audio_composer, audioInfo.composer)
            }
            if (audioInfo.discNumber != null) {
                addItemView(R.string.file_properties_audio_disc_number, audioInfo.discNumber)
            }
            if (audioInfo.trackNumber != null) {
                addItemView(R.string.file_properties_audio_track_number, audioInfo.trackNumber)
            }
            if (audioInfo.year != null) {
                addItemView(R.string.file_properties_audio_year, audioInfo.year)
            }
            if (audioInfo.genre != null) {
                addItemView(R.string.file_properties_audio_genre, audioInfo.genre)
            }
            if (audioInfo.duration != null) {
                addItemView(R.string.file_properties_media_duration, audioInfo.duration.format())
            }
            if (audioInfo.bitRate != null) {
                addItemView(
                    R.string.file_properties_media_bit_rate, getString(
                        R.string.file_properties_media_bit_rate_format, audioInfo.bitRate / 1000
                    )
                )
            }
            if (audioInfo.sampleRate != null) {
                addItemView(
                    R.string.file_properties_audio_sample_rate, getString(
                        R.string.file_properties_audio_sample_rate_format, audioInfo.sampleRate
                    )
                )
            }
        }
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean =
            file.mimeType.isAudio && file.path.isMediaMetadataRetrieverCompatible
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs
}
