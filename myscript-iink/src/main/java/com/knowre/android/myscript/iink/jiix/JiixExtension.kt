package com.knowre.android.myscript.iink.jiix

import android.content.Context
import android.graphics.RectF
import com.knowre.android.myscript.iink.view.candidate.getPreDefinedCandidates
import java.util.Collections


//region Jiix
internal fun Jiix.getAllExpressions(): List<Expression> {
    fun List<Expression>.flatten(): List<Expression> =
        this + this.flatMap { it.operands?.flatten() ?: Collections.emptyList() }

    return this.expressions?.flatten() ?: Collections.emptyList()
}

internal fun Jiix.getCandidates(item: Item): List<String> {
    val iinkCandidates = findExpressionMatches(item.boundingBox)
        ?.getCandidates(item.label)
        ?: listOf()
    return (iinkCandidates + getPreDefinedCandidates(item.label))
        .distinct()
}

internal fun Jiix.getAllSymbols() =
    getAllExpressions()
        .flatMap {
            it.symbols ?: Collections.emptyList()
        }

internal fun Jiix.getAllItems() =
    (items ?: Collections.emptyList()) +
        getAllExpressions()
            .flatMap { it.items ?: Collections.emptyList() }

internal fun Jiix.firstItemOf(id: String) =
    getAllItems().first { it.id == id }

internal fun Jiix.findAllCollidingItems(
    context: Context,
    touchDownX: Float, touchDownY: Float
) = getAllItems()
    .filter { it.isInclude(context, touchDownX, touchDownY) }

internal fun Jiix.findExpressionMatches(boundingBox: BoundingBox) =
    getAllExpressions()
        .filter { exp ->
            exp.items
                ?.any { it.boundingBox == boundingBox }
                ?: false
        }
        .find { it.symbols != null }
//endregion

//region Expression
internal fun Expression.getCandidates(label: String) =
    (symbols
        ?.find { label == it.symbol }
        ?.candidates
        ?: listOf())
//endregion

//region BoundingBox
internal fun BoundingBox.isInclude(
    context: Context,
    x: Float, y: Float
) = transformToRect(context).contains(x, y)


internal fun BoundingBox.transformToRect(context: Context) = RectF(
    mmToPx(context, x - 1F),
    mmToPx(context, y - 1F),
    mmToPx(context, x + width + 1F),
    mmToPx(context, y + height + 1F),
)
//endregion

//region Item
internal fun Item.isInclude(
    context: Context,
    x: Float, y: Float
) = boundingBox.isInclude(context, x, y)
//endregion

//region mm <-> px
internal fun mmToPx(context: Context, mm: Float): Float {
    val metrics = context.resources.displayMetrics
    return mm / 25.4f * metrics.xdpi
}

internal fun mmToDp(context: Context, mm: Float): Float {
    val metrics = context.resources.displayMetrics
    return mm / 25.4f * (metrics.xdpi / 160f)
}
//endregion
