package com.knowre.android.extension.standard


/**
 * @return [predicate] 에 매칭되는 요소가 있을 경우 해당 요소의 index 를 리턴, 매칭되는 요소가 없을 경우 null 을 리턴
 */
fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    return indexOfFirst(predicate).run { if (this == -1) null else this }
}
