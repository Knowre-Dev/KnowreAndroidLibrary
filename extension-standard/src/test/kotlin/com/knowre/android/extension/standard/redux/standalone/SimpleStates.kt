package com.knowre.android.extension.standard.redux.standalone

import com.knowre.android.extension.standard.redux.ReduceAction
import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.state.ReduceOnce
import com.knowre.android.extension.standard.redux.state.ReducibleValue
import com.knowre.android.extension.standard.redux.update
import com.knowre.android.extension.standard.redux.updateOnce

internal data class SimpleState(
    val owner: ReduceOnce<String> =
        ReducibleValue.Uninitialized,
    val count: ReducibleValue<Int> =
        ReducibleValue.Uninitialized
) : Reducible

internal class SimpleStateReduceSpec(snapshot: SimpleState) :
    ReduceSpec<SimpleState>(snapshot) {

    private var ownerOp: ReduceOperation<String> = ReduceOperation.Keep
    var owner by snapshot.owner.reduceOnceProperty(::ownerOp::set)

    private var countOp: ReduceOperation<Int> = ReduceOperation.Keep
    var count by snapshot.count.reduceProperty(::countOp::set)

    override fun SimpleState.apply(): SimpleState = this
        .updateOnce(ownerOp) { new ->
            copy(owner = new)
        }
        .update(countOp) { new ->
            copy(count = new)
        }
}

internal interface SimpleStateReduceAction :
    ReduceAction<SimpleState, SimpleStateReduceSpec>