package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.Composable
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.delay

fun unwrapContext(context: Context): Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

suspend fun captureComposableAsBitmap(context: Context, content: @Composable () -> Unit): Bitmap {
    val composeView = ComposeView(context).apply {
        layoutParams = android.view.ViewGroup.LayoutParams(1080, 1920)
        setContent {
            content()
        }
    }
    
    val activity = unwrapContext(context)
    val rootView = activity?.findViewById<ViewGroup>(android.R.id.content)
    
    if (rootView != null) {
        // Place view far off-screen so it's not visible
        composeView.x = 10000f
        composeView.y = 10000f
        rootView.addView(composeView)
    }

    val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
    composeView.measure(widthSpec, heightSpec)
    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

    // Wait a little bit for the Compose content to fully render
    delay(200) 
    
    val bitmap = composeView.drawToBitmap()
    
    if (rootView != null) {
        rootView.removeView(composeView)
    }
    
    return bitmap
}
