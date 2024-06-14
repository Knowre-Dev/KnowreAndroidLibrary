package com.knowre.android.extension.standard.redux.standalone

import com.knowre.android.extension.standard.redux.SpecBasedReducer
import com.knowre.android.extension.standard.redux.state.current
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class SpecBasedReducerTest {

    private data class SetCount(private val count: Int) : SimpleStateReduceAction {
        override val operation: SimpleStateReduceSpec.() -> Unit
            get() = {
                count = this@SetCount.count
            }
    }

    private data class SetOwner(private val owner: String) : SimpleStateReduceAction {
        override val operation: SimpleStateReduceSpec.() -> Unit
            get() = {
                owner = this@SetOwner.owner
            }
    }

    private lateinit var reducer: SpecBasedReducer<SimpleState, SimpleStateReduceSpec>
    private lateinit var state: SimpleState

    @Before
    fun setup() {
        reducer = object : SpecBasedReducer<SimpleState, SimpleStateReduceSpec>(
            ::SimpleStateReduceSpec
        ) {}
        state = SimpleState()
    }

    @Test
    fun reducer() {
        var newCount = 10
        state = reducer(state, SetCount(newCount))
        assertThat(state.count.current, `is`(newCount))

        newCount = 20
        state = reducer(state, SetCount(newCount))
        assertThat(state.count.current, `is`(newCount))

        var newOwner = "owner"
        state = reducer(state, SetOwner(newOwner))
        assertThat(state.owner.current, `is`(newOwner))

        newOwner = "thief"
        val result = runCatching {
            reducer(state, SetOwner(newOwner))
        }
        assertThat(result.isFailure, `is`(true))
    }
}