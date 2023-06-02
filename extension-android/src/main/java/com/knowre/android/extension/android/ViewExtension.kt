package com.knowre.android.extension.android

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Px
import androidx.core.content.getSystemService
import androidx.core.graphics.toRectF
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding


/**
 * 디바이스 전체 스크린 기준으로 해당 뷰가 위치한 좌표 정보를 저장한 [RectF]
 */
val View.rectF: RectF
    get() = IntArray(2)
        .apply { getLocationOnScreen(this) }
        .let { (left, top) -> Rect(left, top, width + left, height + top) }
        .toRectF()

inline fun View.doOnPostLayout(crossinline action: (View) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                action(this@doOnPostLayout)
            }
        }
    )
}

inline fun View.doOnPostLayoutEager(crossinline action: (View) -> Unit) {
    if (ViewCompat.isLaidOut(this) && !this.isLayoutRequested) {
        action(this)
    } else {
        doOnPostLayout(action)
    }
}

fun View.setMargins(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int) {
    updateLayoutParams<MarginLayoutParams> { setMargins(left, top, right, bottom) }
}

fun View.setMarginLeft(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { leftMargin = margin }
}

fun View.setMarginTop(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { topMargin = margin }
}

fun View.setMarginRight(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { rightMargin = margin }
}

fun View.setMarginBottom(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { bottomMargin = margin }
}

fun View.setMarginHorizontal(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { leftMargin = margin; rightMargin = margin }
}

fun View.setMarginVertical(@Px margin: Int) {
    updateLayoutParams<MarginLayoutParams> { topMargin = margin; bottomMargin = margin }
}

fun View.setSize(@Px width: Int, @Px height: Int) {
    updateLayoutParams {
        this.width = width
        this.height = height
    }
}

fun View.setWidthCompat(@Px width: Int) {
    updateLayoutParams { this.width = width }
}

fun View.setHeightCompat(@Px height: Int) {
    updateLayoutParams { this.height = height }
}

fun View.updatePaddingHorizontal(@Px padding: Int) {
    updatePadding(left = padding, right = padding)
}

fun View.updatePaddingVertical(@Px padding: Int) {
    updatePadding(top = padding, bottom = padding)
}

fun View.hideKeyboard() {
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    context.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
}