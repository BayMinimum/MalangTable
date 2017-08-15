package me.baymallow.malangtable

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.webkit.WebChromeClient
import android.webkit.WebView

import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.content_download.*

class DownloadActivity : AppCompatActivity() {
    lateinit var mChromeClient: MyChromeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mChromeClient = MyChromeClient()

        download_fab.setOnClickListener {
            download_webview.loadUrl("http://students.ksa.hs.kr/scmanager/stuweb/kor/sukang/state.jsp")
            mChromeClient.flag = true
        }

        download_webview.settings.javaScriptEnabled = true
        download_webview.webChromeClient = mChromeClient
        download_webview.loadUrl("http://students.ksa.hs.kr")

        Snackbar.make(download_fab, R.string.press_after_login, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, {}).show()

    }

}

class MyChromeClient : WebChromeClient() {
    var flag = false

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (flag) {
            TODO("Get timetable data from HTML")
        }
    }

}