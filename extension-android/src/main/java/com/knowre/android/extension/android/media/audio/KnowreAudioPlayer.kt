package com.knowre.android.extension.android.media.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.knowre.android.extension.android.media.audio.dto.KnowreAudioPlayerState
import com.knowre.android.extension.android.media.audio.dto.isPlayable
import com.knowre.android.extension.standard.delegate.ObservableProperty


/**
 * MediaPlayer 를 한번 더 감싼 [음성][AudioAttributes.CONTENT_TYPE_MUSIC] Player.
 *
 * 외부에서 MediaPlayer 를 직접 사용하지 않고 한번 더 감싸 만든 이유는 아래와 같다.
 *
 * 1. [MediaPlayer 상태 다이어그램](https://developer.android.com/reference/android/media/MediaPlayer#state-diagram) 을 보면 상태 전이에 대해 알 수 있는데,
 * MediaPlayer 는 이 상태의 값을 외부에서 알 수 없도록 구현되어있어서, 위 상태와 정확히 일치하지는 않지만 그나마 비슷하게 상태를 맞추고 이를 외부로 노출 시키기 위해 [KnowreAudioPlayer] 가 제작되었다.
 * (외부로 노출시키는 상태는 [KnowreAudioPlayerState] 로 정의 하였으며, 이 상태는 [MediaPlayer] 의 내부 상태들에서 Initialized 와 Stopped 를 제외한 상태들이다. 이 둘을 제외한 이유는 이 상태들은 앱을 구현하는데 크게 필요가 없을 것 같아서이다.)
 *
 * 2. Media Player 는 특정 함수를 부를 때 현재 상태가 적절한 상태가 아니면 exception 을 발생시킨다. 예를들어 Idle, Initialized, Prepared, Stopped, Error 상태인데 [MediaPlayer.pause] 함수를 부르면 exception 이 발생한다.
 * 어떤 함수를 부를 때 어떤 상태여야 안전한지는 공홈의 [Valid and Invalid State](https://developer.android.com/reference/android/media/MediaPlayer#valid-and-invalid-states) 를 참고하면된다.
 * 특정함수를 부를 때 위처럼 exception 이 발생하지 않고 안전한 상태에서만 부르게 하고 싶을 수도 있다. 이를 처리하기 위해 [KnowreAudioPlayer] 가 제작되었다. (예, [KnowreAudioPlayer.pause] 에 보면 [MediaPlayer.isPlaying] 으로 검사 후 pause 를 실행한다.)
 * (다만, 이 클래스가 위 공홈 내용의 모든 부분들을 커버하고 있지는 않다.)
 *
 * 3. [MediaPlayer] 의 수많은 기능 중에서 현재 이 클래스에서만 노출 시키고 있는 함수만을 컴팩트하게 사용하기 위해 [KnowreAudioPlayer] 가 제작되었다.
 *
 * MediaPlayer 에는 내부적으로 async 한 작업들이 있기때문에 [KnowreAudioPlayer.state] 가 [MediaPlayer] 의 내부 상태와 정확한 타이밍에 정확히 일치되지는 않는다.
 * 하지만, 그 타이밍의 차이가 매우 사소하며 앱을 구현하는데 있어서는 큰 무리가 없다.
 *
 * 더 자세한 사항은 공홈을 참고하면 좋다.
 *
 * @see KnowreAudioPlayerState
 */
class KnowreAudioPlayer {

    var playWhenPrepared: Boolean = true

    var state: KnowreAudioPlayerState by ObservableProperty(
        initialValue = KnowreAudioPlayerState.Idle(shouldReset = false),
        beforeChange = { _, old, new -> old::class != new::class }
    ) { _, old, new ->
        new.applyState()
        listener?.onStateChanged(mediaPlayer, url, old, new)
    }
        private set

    var url: String? = null
        private set

    var listener: KnowreAudioPlayerListener? = null

    val currentPosition: Int get() = mediaPlayer.currentPosition

    val duration: Int get() = mediaPlayer.duration

    private var mediaPlayer: MediaPlayer = createMediaPlayer()

    /**
     * 이 함수는 단순히 media 를 재생만 하는 것이 아니라 플레이어를 초기화([Idle][KnowreAudioPlayerState.Idle]) 및 media 준비([Preparing][KnowreAudioPlayerState.Preparing])를 거쳐 최종적으로 재생하는 것까지 포함한다.
     *
     * 만약 플레이어가 [Error][KnowreAudioPlayerState.Error], [Release(End)][KnowreAudioPlayerState.End] 상태이거나 url 이 달라졌을 경우에는 자동적으로 새로 초기화 및 media 준비를 한 후 재생한다.
     * 다만, [playWhenPrepared] 가 false 인 값을 가진 상태로 start 함수를 호출하게되면 media 가 준비되더라도 media 가 자동으로 재생되지 않고 [Prepared][KnowreAudioPlayerState.Prepared] 상태로 남게된다.
     * ([KnowreAudioPlayerState.Paused] 와는 다른 상태이다.)
     *
     * [KnowreAudioPlayer] 는 media 를 async 하게 준비하고, media 준비 중에는 상태가 [Preparing][KnowreAudioPlayerState.Preparing] 이 된다.
     * 이 상태일 때 만약 [playWhenPrepared] 가 false 라면 외부에서 start 함수를 여러번 더 부른다고 하더라도 미디어가 준비된 이후에 자동재생된다고 생각하면 안된다.
     * 즉, [playWhenPrepared] 가 false 일 때, start 를 호출해 media 준비가 완료된 이후라면([Prepared][KnowreAudioPlayerState.Prepared]상태) 다시 start 를 호출하면 media 가 재생되지만,
     * start 를 호출한 후 아직 media 가 준비가 안됐을 경우엔([Preparing][KnowreAudioPlayerState.Preparing]상태) 다시 start 를 호출해도 media 준비 완료 후 자동재생되지 않는다.
     * ([Preparing][KnowreAudioPlayerState.Preparing] 중일 때는 [playWhenPrepared] 의 상태를 true 로 바꿔줘야 준비가 완료된 이후 media 가 재생되게 된다.)
     *
     * 참고 : media 재생 중에는 start 를 계속해서 호출하더라도 아무일도 일어나지 않는다.(처음부터 재생되는 것 아님) 만약 재생 중에 다시 처음 부터 재생하길 원할 경우 [seekTo] 를 사용하면 된다.
     */
    fun start(url: String) {
        when (state) {
            is KnowreAudioPlayerState.Error -> state = KnowreAudioPlayerState.Idle(shouldReset = true)
            is KnowreAudioPlayerState.End -> mediaPlayer = createMediaPlayer()
            else -> Unit
        }

        if (this.url != url) {
            this.url = url

            state = KnowreAudioPlayerState.Idle(shouldReset = true)
            state = KnowreAudioPlayerState.Preparing()
        }

        state = when (state) {
            is KnowreAudioPlayerState.Idle -> KnowreAudioPlayerState.Preparing()

            is KnowreAudioPlayerState.Prepared,
            is KnowreAudioPlayerState.Started,
            is KnowreAudioPlayerState.Paused,
            is KnowreAudioPlayerState.PlaybackCompleted -> KnowreAudioPlayerState.Started()

            is KnowreAudioPlayerState.Preparing -> state

            is KnowreAudioPlayerState.Error,
            is KnowreAudioPlayerState.End -> error("$state 는 올 수 없는 상태입니다.")
        }
    }

    fun pause(): Boolean {
        return if (runCatching { mediaPlayer.isPlaying }.run { isSuccess && getOrThrow() }) {
            state = KnowreAudioPlayerState.Paused()
            true
        } else {
            false
        }
    }

    fun resume() {
        if (state is KnowreAudioPlayerState.Idle || state is KnowreAudioPlayerState.Preparing) {
            return
        }

        state = KnowreAudioPlayerState.Started()
    }

    fun release() {
        state = KnowreAudioPlayerState.End()
    }

    fun seekTo(position: Int) {
        if (state.isPlayable) mediaPlayer.seekTo(position)
    }

    private fun KnowreAudioPlayerState.applyState() {
        when (this) {
            is KnowreAudioPlayerState.Idle -> if (shouldReset) mediaPlayer.reset()
            is KnowreAudioPlayerState.Preparing -> with(mediaPlayer) { setDataSource(url); prepareAsync() }
            is KnowreAudioPlayerState.Prepared -> Unit
            is KnowreAudioPlayerState.Started -> mediaPlayer.start()
            is KnowreAudioPlayerState.Paused -> mediaPlayer.pause()
            is KnowreAudioPlayerState.PlaybackCompleted -> Unit
            is KnowreAudioPlayerState.Error -> Unit
            is KnowreAudioPlayerState.End -> mediaPlayer.release()
        }
    }

    private fun createMediaPlayer() = MediaPlayer()
        .apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())

            setOnPreparedListener {
                state = if (playWhenPrepared) {
                    KnowreAudioPlayerState.Started()
                } else {
                    KnowreAudioPlayerState.Prepared()
                }
            }

            setOnCompletionListener { state = KnowreAudioPlayerState.PlaybackCompleted() }

            setOnErrorListener { _, what, extra ->
                /**
                 * 정보 : what 이 -38 일 경우에는 preparing 중에 [MediaPlayer.seekTo] 나 [MediaPlayer.start] 와 같이
                 * prepared 된 후에 할 수 있을 만한 작업을 했을 경우가 포함된다.
                 */
                state = KnowreAudioPlayerState.Error(what = what, extra = extra)
                /** true 를 리턴해야 error 발생 시 onCompleteListener 가 불리지 않는다 */
                true
            }
        }
        .also {
            /**
             * 새로운 [MediaPlayer] 객체가 생성되면 기본 상태가 Idle 이므로 [MediaPlayer.reset] 을 부를 필요가 없기 때문에 [KnowreAudioPlayerState.Idle.shouldReset] 을 false 로 설정한다.
             * 기존 [MediaPlayer] 에 [MediaPlayer.reset] 을 불러서 Idle 상태가 되는 것과 [MediaPlayer] 의 새로운 객체를 생성해서 Idle 상태가되는 것에는 매우 근소한 차이가 있는데 이는 공홈을 참고.
             */
            state = KnowreAudioPlayerState.Idle(shouldReset = false)
        }

}