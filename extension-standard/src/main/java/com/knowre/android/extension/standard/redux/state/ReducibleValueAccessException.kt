package com.knowre.android.extension.standard.redux.state

class ReducibleValueAccessException internal constructor(message: String) :
    RuntimeException(message)

@PublishedApi
internal fun ReducibleValue.Companion.error(message: String): Nothing =
    throw ReducibleValueAccessException(message)

internal const val UNINITIALIZED_VALUE_ACCESS_EXCEPTION: String =
    "ReducibleValue must be initialized before get."