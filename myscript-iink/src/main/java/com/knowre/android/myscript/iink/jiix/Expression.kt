package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


data class Expression(
    val id: String?,
    val type: String,
    val items: List<Item>?,
    @SerializedName("open symbol")
    val openSymbol: String?,
    @SerializedName("close symbol")
    val closeSymbol: String?,
    @SerializedName("bounding-box")
    val boundingBox: BoundingBox?,
    val symbols: List<Symbol>?,
    val operands: List<Expression>?
)

internal fun Expression.changeItem(newItem: Item): Expression =
    this.copy(
        items = items?.map { item ->
            if (item.id == newItem.id) newItem else item
        },
        operands = operands?.map { operand ->
            operand.changeItem(newItem)
        }
    )
