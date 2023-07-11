package com.knowre.android.extension.android.media.audio.dto

import android.media.MediaPlayer


/**
 * [MediaPlayer 상태 다이어그램](https://developer.android.com/reference/android/media/MediaPlayer#state-diagram) 의 상태 중 Initialized 와 Stopped 를 제외한 상태.
 * [MediaPlayer] 는 자신의 내부 상태 값을 외부로 노출 시키고 있지 않기 때문에, 부득이하게 [KnowreAudioPlayerState] 를 커스텀하게 구현.
 * Initialized 와 Stopped 두 상태를 제외한 이유는 이 상태들은 딱히 앱을 구현하는데 필요가 없고, 괜히 추가하면 더 복잡성을 높일 것 같기 때문
 */
sealed class KnowreAudioPlayerState {
    data class Idle(val name: String = "Idle", val shouldReset: Boolean) : KnowreAudioPlayerState()

    data class Preparing(val name: String = "Preparing") : KnowreAudioPlayerState()

    data class Prepared(val name: String = "Prepared") : KnowreAudioPlayerState()

    data class Started(val name: String = "Started") : KnowreAudioPlayerState()

    data class Paused(val name: String = "Paused") : KnowreAudioPlayerState()

    data class PlaybackCompleted(val name: String = "PlaybackCompleted") : KnowreAudioPlayerState()

    data class Error(val name: String = "Error", val what: Int, val extra: Int) : KnowreAudioPlayerState()

    data class End(val name: String = "End") : KnowreAudioPlayerState()
}

val KnowreAudioPlayerState.isPlayable
    get() = this is KnowreAudioPlayerState.Prepared
        || this is KnowreAudioPlayerState.Started
        || this is KnowreAudioPlayerState.Paused
        || this is KnowreAudioPlayerState.PlaybackCompleted

val KnowreAudioPlayerState.isIdle
    get() = this is KnowreAudioPlayerState.Idle

val KnowreAudioPlayerState.isPreparing
    get() = this is KnowreAudioPlayerState.Preparing

val KnowreAudioPlayerState.isPrepared
    get() = this is KnowreAudioPlayerState.Prepared

val KnowreAudioPlayerState.isStarted
    get() = this is KnowreAudioPlayerState.Started

val KnowreAudioPlayerState.isPaused
    get() = this is KnowreAudioPlayerState.Paused

val KnowreAudioPlayerState.isPlaybackCompleted
    get() = this is KnowreAudioPlayerState.PlaybackCompleted

val KnowreAudioPlayerState.isError
    get() = this is KnowreAudioPlayerState.Error

val KnowreAudioPlayerState.isReleased
    get() = this is KnowreAudioPlayerState.End