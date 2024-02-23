package com.knowre.android.myscript.iink.jiix

import com.google.gson.annotations.SerializedName


data class Expression(
    val id: String,
    val type: String,
    val items: List<Item>,
    @SerializedName("open symbol")
    val openSymbol: String,
    @SerializedName("close symbol")
    val closeSymbol: String,
    @SerializedName("bounding-box")
    val boundingBox: BoundingBox,
    val operands: List<Expression>
)