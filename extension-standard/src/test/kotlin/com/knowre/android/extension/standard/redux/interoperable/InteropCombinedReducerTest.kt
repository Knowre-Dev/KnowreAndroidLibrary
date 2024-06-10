package com.knowre.android.extension.standard.redux.interoperable

import com.knowre.android.extension.standard.redux.state.current
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class InteropCombinedReducerTest {

    private lateinit var reducer: Reducer<CombinedState, CombinedAction>
    private lateinit var state: CombinedState

    @Before
    fun setup() {
        reducer = CombinedReducer()
        state = CombinedState()
    }

    @Test
    fun interoperableCombinedReducer() {
        val newCombined = true
        val newFooViewStateVal = true
        val newFooVal1 = 10
        val newFooVal2 = "FooVal2"
        val newBarViewStateVal = 22
        val newBarVal = true
        val newBaz = 40
        val update = UpdateCombinedState(
            newCombined,
            newFooViewStateVal, newFooVal1, newFooVal2,
            newBarViewStateVal, newBarVal,
            newBaz,
        )
        state = reducer.reduce(state, update)
        assertThat(state.viewState.combined.current, `is`(newCombined))
        assertThat(state.fooState.viewState.fooViewStateVal.current, `is`(newFooViewStateVal))
        assertThat(state.fooState.fooVal1.current, `is`(newFooVal1))
        assertThat(state.fooState.fooVal2.current, `is`(newFooVal2))
        assertThat(state.barState.viewState.barViewStateVal.current, `is`(newBarViewStateVal))
        assertThat(state.barState.barVal.current, `is`(newBarVal))
        assertThat(state.baz.current, `is`(newBaz))
    }
}