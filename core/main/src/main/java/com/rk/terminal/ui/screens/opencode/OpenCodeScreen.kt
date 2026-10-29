package com.rk.terminal.ui.screens.opencode

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.HttpAuthHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OpenCodeScreen() {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = object : WebViewClient() {
                override fun onReceivedHttpAuthRequest(
                    view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?
                ) {
                    if (host == "127.0.0.1" || host == "localhost") {
                        handler?.proceed("opencode", "andlinux-local-opencode")
                    } else {
                        handler?.cancel()
                    }
                }
            }
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            loadUrl("http://127.0.0.1:4123")
        }
    }
    DisposableEffect(webView) {
        onDispose { webView.stopLoading(); webView.destroy() }
    }
    AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize(), update = {
        it.layoutParams = ViewGroup.LayoutParams(-1, -1)
    })
}
