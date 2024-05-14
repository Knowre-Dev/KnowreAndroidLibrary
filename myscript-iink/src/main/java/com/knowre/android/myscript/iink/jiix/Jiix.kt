package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


data class Jiix(
    val id: String,
    val type: String,
    val items: List<Item>?,
    val expressions: List<Expression>?,
    @SerializedName("bounding-box")
    val boundingBox: BoundingBox?,
    val version: String
)

fun Jiix.isValid() =
    !expressions.isNullOrEmpty() &&
        expressions.all { it.id != null } &&
        getAllItems().all { it.isValid }

fun Jiix.changeItem(itemId: String, func: Item.() -> Item) =
    changeItem(firstItemOf(itemId).func())

internal fun Jiix.changeItem(newItem: Item) =
    this.copy(
        items = items?.map { item ->
            if (item.id == newItem.id) newItem else item
        },
        expressions = expressions?.map { expression ->
            expression.changeItem(newItem)
        }
    )
