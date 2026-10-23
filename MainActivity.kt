package com.albab.realestate

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.*
import android.view.ViewGroup
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var chooserCallback: ValueCallback<Array<Uri>>? = null
    private val chooserRequest = 701

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(view: WebView?, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams?): Boolean {
                chooserCallback?.onReceiveValue(null)
                chooserCallback = callback
                val intent = params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
                return try { startActivityForResult(intent, chooserRequest); true }
                catch (_: Exception) { chooserCallback = null; false }
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return if (url.startsWith("file:///android_asset/") || url.startsWith("https://")) false else true
            }
        }
        web.setDownloadListener { url, _, _, _, _ ->
            // The app's backup export is generated as a Blob download. Android WebView may not expose it as a normal URL;
            // keep the app stable and let the user use the in-app restore/export where supported.
        }
        setContentView(FrameLayout(this).apply { addView(web) })
        if (savedInstanceState == null) web.loadUrl("file:///android_asset/index.html") else web.restoreState(savedInstanceState)
    }

    @Deprecated("Deprecated in Android SDK but broadly compatible")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooserRequest) {
            val result = if (resultCode == Activity.RESULT_OK && data != null) WebChromeClient.FileChooserParams.parseResult(resultCode, data) else null
            chooserCallback?.onReceiveValue(result)
            chooserCallback = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
