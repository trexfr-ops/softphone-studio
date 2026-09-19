package com.softphone.studio.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.softphone.studio.theme.OledBlack
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Interface exposed to the inlined JavaScript runtime in startup_splash.html.
 * Dispatches completion callbacks strictly to the Android Main Looper.
 */
private class SplashBridge(
    private val onComplete: () -> Unit
) {
    @JavascriptInterface
    fun onFinish() {
        Handler(Looper.getMainLooper()).post {
            onComplete()
        }
    }
}

/**
 * Flagship hardware-accelerated startup splash screen for PhantomLine.
 * Renders the web-identical Anime.js + SVG vector telemetry animation from assets.
 *
 * @param onFinish Callback invoked when animation completes, user taps, or safety timeout fires.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonoLuxurySplashScreen(
    onFinish: () -> Unit
) {
    val isFinished = remember { AtomicBoolean(false) }

    val finishSafely = remember {
        {
            if (isFinished.compareAndSet(false, true)) {
                onFinish()
            }
        }
    }

    // Safety timeout: guarantees progression even on low-end devices or Webview initialization delays
    LaunchedEffect(Unit) {
        delay(4000L)
        finishSafely()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = finishSafely
            ),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.BLACK)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }

                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()

                    addJavascriptInterface(SplashBridge(finishSafely), "AndroidBridge")
                    loadUrl("file:///android_asset/startup_splash.html")
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.removeJavascriptInterface("AndroidBridge")
                webView.destroy()
            }
        )
    }
}
