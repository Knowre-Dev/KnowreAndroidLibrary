package com.knowre.android.extension.android.media.audio

import android.media.MediaPlayer
import com.knowre.android.extension.android.media.audio.dto.KnowreAudioPlayerState


interface KnowreAudioPlayerListener {
    fun onStateChanged(mp: MediaPlayer, url: String?, old: KnowreAudioPlayerState, new: KnowreAudioPlayerState)
}