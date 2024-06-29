package com.knowre.android.extension.standard.redux.standalone

import com.knowre.android.extension.standard.redux.state.current
import com.knowre.android.extension.standard.redux.state.peek
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class ReduceSpecTest {

    private lateinit var state: SimpleState

    @Before
    fun setup() {
        state = SimpleState()
    }

    @Test
    fun peek() {
        val owner = state.owner.peek().getOrNull()
        assertThat(owner, nullValue())
        val counter = state.owner.peek().getOrNull()
        assertThat(counter, nullValue())
    }

    @Test
    fun reduce() {
        state = SimpleStateReduceSpec(state)
            .apply {
                count = 1
            }
            .invoke()
        assertThat(state.count.current, `is`(1))

        state = SimpleStateReduceSpec(state)
            .apply {
                count = 2
                owner = "owner"
            }
            .invoke()
        assertThat(state.count.current, `is`(2))
        assertThat(state.owner.current, `is`("owner"))
    }

    @Test
    fun reduceOnce() {
        state = SimpleStateReduceSpec(state)
            .apply {
                owner = "owner"
            }
            .invoke()
        assertThat(state.owner.current, `is`("owner"))

        val result: Result<SimpleState> = runCatching {
            SimpleStateReduceSpec(state)
                .apply {
                    owner = "thief"
                }
                .invoke()
        }
        assertThat(result.isFailure, `is`(true))
    }
}