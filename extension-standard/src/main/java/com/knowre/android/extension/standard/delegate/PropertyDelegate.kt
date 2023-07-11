package com.knowre.android.extension.standard.delegate

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * 코틀린 기본 [ObservableProperty] 와 근본적으로 같지만,
 * 코틀린 기본 [ObservableProperty] 는 [ObservableProperty.beforeChange] 값이 항상 true 이기 때문에 같은 값을 할당하더라도 [ObservableProperty.afterChange] 가 불리게 되고,
 * 이 [ObservableProperty] 는 기존 값과는 다른 값을 할당 했을 경우에만 [ObservableProperty.afterChange] 가 불린다.
 */
inline fun <T : Any?> ObservableProperty(
    initialValue: T,
    crossinline beforeChange: (KProperty<*>, T, T) -> Boolean = { _, old, new -> old != new },
    crossinline afterChange: (KProperty<*>, T, T) -> Unit
): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        afterChange(property, oldValue, newValue)
    }

    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
        return beforeChange(property, oldValue, newValue)
    }
}