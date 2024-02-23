package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


data class Jiix(
    val id: String,
    val type: String,
    val items: List<Item>,
    val expressions: List<Expression>,
    @SerializedName("bounding-box")
    val boundingBox: BoundingBox,
    val version: String
)