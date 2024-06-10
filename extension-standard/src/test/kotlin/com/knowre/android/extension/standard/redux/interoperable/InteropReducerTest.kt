package com.knowre.android.extension.standard.redux.interoperable

import com.knowre.android.extension.standard.redux.state.current
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class InteropReducerTest {

    private lateinit var reducer: Reducer<InteroperableState, InteroperableAction>
    private lateinit var state: InteroperableState

    @Before
    fun setup() {
        reducer = InteroperableReducer()
        state = InteroperableState()
    }

    @Test
    fun interoperableReducer() {
        assertThat(state.referenceCount.current, `is`(0))

        val newCount = 10
        val setCount = InteroperableAction.Callback(
            InteroperableCallbackAction.SetCount(newCount)
        )
        state = reducer.reduce(state, setCount)
        assertThat(state.viewState.count.current, `is`(newCount))
        assertThat(state.referenceCount.current, `is`(1))

        val newOwner = "owner"
        val setOwner = InteroperableAction.Callback(
            InteroperableCallbackAction.SetOwner(newOwner)
        )
        state = reducer.reduce(state, setOwner)
        assertThat(state.viewState.count.current, `is`(newCount))
        assertThat(state.viewState.owner.current, `is`(newOwner))
        assertThat(state.referenceCount.current, `is`(2))

        state = reducer.reduce(state, InteroperableAction.Reset)
        assertThat(state.viewState.count.current, `is`(0))
        assertThat(state.viewState.owner.current, `is`(newOwner))
        assertThat(state.referenceCount.current, `is`(0))
    }
}