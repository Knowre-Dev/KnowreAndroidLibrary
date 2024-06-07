package com.knowre.android.extension.standard.state

import com.knowre.android.extension.standard.state.StateProperty.Uninitialized
import kotlin.reflect.KProperty

/**
 * [Uninitialized] 인 [StateProperty] 의 [값][StateProperty.Data.value] 에
 * 접근하는 경우 발생하는 [Exception][RuntimeException].
 */
class UninitializedStatePropertyAccessException internal constructor(
    message: String? = null
) : RuntimeException(message)

/**
 * [StateProperty] 접근을 간결하게 하기 위한 확장 함수. 만약 해당 프로퍼티가 [Uninitialized] 라면
 * [UninitializedStatePropertyAccessException] 을 `throw` 한다.
 *
 * ```kotlin
 * data class SomeState(
 *     val importantStateProp: StateProperty<Int> = Uninitialized
 * ) {
 *     val importantState: Int by importantStateProp
 *
 *     val importantStateVerbose: Int
 *         get() = if (importantStateProp is StateProperty.Data) {
 *             importantStateProp.value
 *         } else throw UninitializedStatePropertyAccessException(
 *             "StateProperty \"importantStateProp\" " +
 *                 "must be initialized before get."
 *         }
 * }
 *
 * fun doSomething() {
 *     val state = SomeState(Uninitialized)
 *     val current = state.importantState // throw Exception
 * }
 * ```
 * [StateProperty] 를 [안전하게][Result] 접근할 필요가 있다면, [StateProperty.boxed] 참고.
 */
operator fun <T> StateProperty<T>.getValue(
    thisRef: Any?,
    kProperty: KProperty<*>
): T = when (this) {
    Uninitialized -> throw UninitializedStatePropertyAccessException(
        "StateProperty \"${kProperty.name}\" must be initialized before get."
    )

    is StateProperty.Data -> value
}

/**
 * [StateProperty] 접근 결과를 [Result] 로 래핑하는 확장 프로퍼티.
 *
 * ```kotlin
 * data class SomeState(
 *     val importantStateProp: StateProperty<Int> = Uninitialized
 * ) {
 *     val importantState: Result<Int> by importantStateProp::boxed
 *     // ... OR
 *     val importantStateGet: Result<Int>
 *         get() = importantStateProp.boxed
 * }
 *
 * fun doSomething() {
 *     val state = SomeState(Uninitialized)
 *     val current = state.importantState
 *     println(current) // Result.Failure(UninitializedStatePropertyAccessException)
 * }
 * ```
 */
val <T> StateProperty<T>.boxed: Result<T>
    get() = runCatching {
        (this as? StateProperty.Data)?.value
            ?: throw UninitializedStatePropertyAccessException()
    }