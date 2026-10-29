package com.rk.terminal.ui.screens.opencode

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.HttpAuthHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
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
    var serverReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repeat(60) {
            val ready = withContext(Dispatchers.IO) {
                runCatching {
                    (URL("http://127.0.0.1:4123").openConnection() as HttpURLConnection).run {
                        connectTimeout = 500
                        readTimeout = 500
                        requestMethod = "GET"
                        connect()
                        responseCode in 200..499
                    }
                }.getOrDefault(false)
            }
            if (ready) {
                serverReady = true
                return@LaunchedEffect
            }
            delay(500)
        }
    }

    if (!serverReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("Starting OpenCode…", color = ComposeColor.White)
            }
        }
        return
    }

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
            // OpenCode v2 consumes this token in the SPA and keeps the
            // credentials in memory, so no server URL or password prompt is
            // shown to the user.
            loadUrl("http://127.0.0.1:4123/?auth_token=b3BlbmNvZGU6YW5kbGludXgtbG9jYWwtb3BlbmNvZGU")
        }
    }
    DisposableEffect(webView) {
        onDispose { webView.stopLoading(); webView.destroy() }
    }
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize(), update = {
            it.layoutParams = ViewGroup.LayoutParams(-1, -1)
        })
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = ComposeColor(0xEE202124)
        ) {
            Column(Modifier.padding(10.dp)) {
                Text("OpenCode server", color = ComposeColor.White)
                Text("URL: http://127.0.0.1:4123", color = ComposeColor.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Password: andlinux-local-opencode", color = ComposeColor.White, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("OpenCode password", "andlinux-local-opencode"))
                    }) { Text("Copy") }
                }
            }
        }
    }
}
