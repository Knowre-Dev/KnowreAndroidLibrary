package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


class Item(
    val type: String,
    val id: String,
    val timestamp: String,
    val label: String,
    @SerializedName("bounding-box")
    val boundingBox: BoundingBox
)