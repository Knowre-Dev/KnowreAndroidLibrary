package com.knowre.android.extension.standard

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


@Suppress("TestFunctionName", "NonAsciiCharacters")
internal class ListExtensionIndexOfFirstOrNullTest {

    @Test
    fun 매칭되는_요소를_찾았을_경우_해당_요소의_인덱스_리턴() {
        val list = listOf(1, 2)
        val expected = 1
        val actual = list.indexOfFirstOrNull { it == 2 }
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun 매칭되는_요소를_찾지_못했을_경우_null_리턴() {
        val list = listOf(1, 2)
        val expected = null
        val actual = list.indexOfFirstOrNull { it == 4 }
        assertThat(actual).isEqualTo(expected)
    }

}