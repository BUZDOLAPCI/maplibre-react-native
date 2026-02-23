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
     * Prevent the parent MapView (and any ancestor gesture-based containers) from
     * intercepting touch events once a touch starts inside this marker content.
     *
     * Without this, the MapView's gesture detector can steal ACTION_MOVE events
     * for map panning before React Native's JS-side Pressable components have a
     * chance to claim the responder role (the JS evaluation is asynchronous via
     * Fabric/JSI). The result is ACTION_CANCEL being sent to children, causing
     * `onPress` to never fire on Pressable buttons inside the marker.
     *
     * This is the standard Android pattern for nested interactive views inside
     * scrollable/gesture-based containers (analogous to a Button inside ScrollView).
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
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
