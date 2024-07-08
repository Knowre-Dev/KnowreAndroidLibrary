package com.knowre.android.extension.standard.redux.interoperable.hexagon

import com.knowre.android.extension.standard.redux.state.ReducibleValue
import com.knowre.android.extension.standard.redux.state.current
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class HexagonInteropReducerTest {

    private lateinit var reducer: HexagonReducer<ExampleState>
    private lateinit var state: ExampleState

    @Before
    fun setup() {
        reducer = ExampleReducer()
        state = ExampleState()
    }

    @Test
    fun interoperableReducer() {
        assertThat(state.userId, `is`(ReducibleValue.Uninitialized))
        state = reducer.reduce(state, action = ExampleMiddlewareAction.UpdateUserId(newUserId = 999))
        assertThat(state.userId.current, `is`(999))

        assertThat(state.viewState.fooScreenState.label.current, `is`(""))
        state = reducer.reduce(state, action = ExampleCallbackAction.OnClickToggleButton)
        assertThat(state.viewState.fooScreenState.label.current, `is`(ExampleCallbackAction.LABEL_CLICKED))

        state = reducer.reduce(state, action = ExampleMiddlewareAction.ShowLoading)
        assertThat(state.viewState.isShowLoading.current, `is`(true))
        state = reducer.reduce(state, action = ExampleMiddlewareAction.HideLoading)
        assertThat(state.viewState.isShowLoading.current, `is`(false))

        val newBarLabel = "강호동"
        val newNumber = listOf(1, 2, 3)
        state = reducer.reduce(state, action = ExampleMiddlewareAction.UpdateBarScreen(newLabel = newBarLabel, newNumbers = newNumber))
        assertThat(state.viewState.barScreenState.label.current, `is`(newBarLabel))
        assertThat(state.viewState.barScreenState.numbers.current, `is`(newNumber))

        val newFooLabel = "이승기"
        val newNames = listOf("A", "B", "C")
        state = reducer.reduce(state, action = ExampleMiddlewareAction.UpdateFooScreen(newLabel = newFooLabel, newNames = newNames))
        assertThat(state.viewState.fooScreenState.label.current, `is`(newFooLabel))
        assertThat(state.viewState.fooScreenState.names.current, `is`(newNames))
    }
}