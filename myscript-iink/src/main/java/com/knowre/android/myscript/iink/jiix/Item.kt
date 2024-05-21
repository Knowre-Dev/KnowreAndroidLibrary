package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


data class Item(
    val id: String,
    val type: String,
    val timestamp: String,
    val label: String,
    @SerializedName("bounding-box")
    val box: BoundingBox?
) {

    val boundingBox: BoundingBox
        get() = box!!

    val isValid: Boolean
        get() = box != null
}

fun Item.changeLabel(label: String): Item {
    return copy(label = label)
}