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
    /**
     * iink 4.5 부터 **미해결 피연산자 자리**(예: `2+` 처럼 한쪽을 비운 입력)를
     * 객체가 아닌 `null` 로 직렬화한다. 3.0.2 는 같은 자리를
     * `{"type":"number","label":"?","generated":true,"error":"Unsolved"}` 객체로 내보냈다.
     *
     * Gson 은 Kotlin 의 non-null 제네릭을 강제하지 않아 타입 선언만으로는 막을 수 없으므로,
     * 원소 타입을 nullable 로 선언하고 순회하는 쪽에서 걸러낸다.
     */
    val operands: List<Expression?>?
)

internal fun Expression.changeItem(newItem: Item): Expression =
    this.copy(
        items = items?.map { item ->
            if (item.id == newItem.id) newItem else item
        },
        operands = operands?.map { operand ->
            operand?.changeItem(newItem)
        }
    )
