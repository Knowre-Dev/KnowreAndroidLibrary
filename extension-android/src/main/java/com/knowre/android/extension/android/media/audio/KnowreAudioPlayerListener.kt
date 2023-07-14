package com.knowre.android.extension.android.media.audio

import android.media.MediaPlayer


fun interface KnowreAudioPlayerListener {
    fun onStateChanged(mp: MediaPlayer, url: String?, old: KnowreAudioPlayerState, new: KnowreAudioPlayerState)
}