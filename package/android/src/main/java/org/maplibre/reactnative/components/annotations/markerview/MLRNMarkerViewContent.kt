package org.maplibre.reactnative.components.annotations.markerview

import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import com.facebook.react.views.view.ReactViewGroup

/**
 * Based on rnmapbox/maps implementation:
 * https://github.com/rnmapbox/maps/blob/512c50865f322fa89e0d20066b4a0fb10f080cb5/android/src/main/java/com/rnmapbox/rnmbx/components/annotation/RNMBXMarkerViewContent.kt
 */
class MLRNMarkerViewContent(
    context: Context,
) : ReactViewGroup(context) {
    init {
        allowRenderingOutside()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        configureParentClipping()
    }

    /**
     * Consume ALL touch events that land inside this marker content view.
     *
     * React Native Fabric processes touches asynchronously: native ACTION_DOWN
     * arrives, gets forwarded to JS via Fabric/JSI, JS runs responder negotiation
     * (onStartShouldSetResponder etc.), and only THEN does JS claim the touch.
     * During this async gap, ReactViewGroup.dispatchTouchEvent returns false
     * (no child consumed the event synchronously). Android interprets this as
     * "child not interested" and gives the entire gesture to the parent MapView,
     * which processes it as a map tap/pan — causing taps to pass through the card.
     *
     * Fix: Always return true from dispatchTouchEvent.
     * - super.dispatchTouchEvent(ev) forwards the event into React Native's
     *   Fabric touch pipeline (which will async-dispatch to JS Pressables).
     * - Returning true tells Android this view consumed the event, preventing
     *   MapView.onTouchEvent from processing it.
     * - requestDisallowInterceptTouchEvent prevents MapView from intercepting
     *   subsequent MOVE/UP events in the gesture sequence.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        super.dispatchTouchEvent(ev)
        return true
    }

    private fun configureParentClipping() {
        val parent = parent
        if (parent is ViewGroup) {
            parent.allowRenderingOutside()
        }
    }

    /**
     * Returns the authoritative content size from the first child view.
     * The child is laid out by React Native before this wrapper is added to the map,
     * so its dimensions are reliable even before this view's own layout pass runs.
     */
    fun getContentSize(): Pair<Float, Float> {
        val child = getChildAt(0) ?: return Pair(width.toFloat(), height.toFloat())
        val w = if (child.width > 0) child.width else width
        val h = if (child.height > 0) child.height else height
        return Pair(w.toFloat(), h.toFloat())
    }
}

private fun ViewGroup.allowRenderingOutside() {
    clipChildren = false
    clipToPadding = false
}
