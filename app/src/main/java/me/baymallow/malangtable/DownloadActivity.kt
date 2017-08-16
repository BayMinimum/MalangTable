@file:Suppress("DEPRECATION")

package me.baymallow.malangtable

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.content_download.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class DownloadActivity : AppCompatActivity() {
    lateinit var mChromeClient: MyChromeClient
    lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mChromeClient = MyChromeClient(this)

        download_fab.setOnClickListener {
            mProgressDialog = createDialog(resources.getString(R.string.retrieving_timetable))
            download_webview.addJavascriptInterface(
                    TimetableGetter(this)
                    , "TimetableGetter")
            download_webview.loadUrl("http://students.ksa.hs.kr/scmanager/stuweb/kor/sukang/state.jsp")

            mChromeClient.flag = true
        }

        download_webview.settings.javaScriptEnabled = true
        download_webview.webChromeClient = mChromeClient
        download_webview.loadUrl("http://students.ksa.hs.kr")

        Snackbar.make(download_fab, R.string.press_after_login, Snackbar.LENGTH_INDEFINITE)
                .show()

    }

    fun createDialog(str: String): ProgressDialog {
        val mDialog = ProgressDialog(this)
        mDialog.isIndeterminate = true
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mDialog.setMessage(str)
        mDialog.show()
        return mDialog
    }


}


class MyChromeClient(val context: DownloadActivity) : WebChromeClient() {
    var flag = false

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (flag and (newProgress == 100)) {
            context.mProgressDialog.dismiss()
            context.mProgressDialog = context.createDialog(
                    context.resources.getString(R.string.extracting_timetable))
            view.loadUrl("javascript:window.TimetableGetter.saveTimetable" +
                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
            flag = false
        }

    }

}

class TimetableGetter(val context: DownloadActivity) {
    lateinit var subjectList: Element
    lateinit var table: Element
    val dayKor = arrayOf("월", "화", "수", "목", "금")
    val tableArray = ArrayList<ArrayList<String>>(0)

    @JavascriptInterface
    @SuppressWarnings
    fun saveTimetable(html: String) {
        val document = Jsoup.parse(html)
        subjectList = document.getElementsByClass("board_list")[0]
        table = document.getElementsByClass("board_view")[1]
        fillClassroomArray()
        var flag = true
        var count = 0
        val mEdit = context
                .getSharedPreferences(context.packageName + ".pref", Context.MODE_PRIVATE)
                .edit()
        for (row in subjectList.getElementsByTag("tr")) {
            if (flag) flag = false
            else {
                val rowElements = row.getElementsByTag("td")
                val title = rowElements[3].text()
                val teacher = rowElements[6].text()
                val classHours = rowElements[7].text()
                val classNumber = Integer.parseInt(rowElements[8].text())
                val classHoursBuilder = StringBuilder()
                var flag2 = true
                var place = ""
                for (hour in classHours.split("|")) {
                    if (!flag2) classHoursBuilder.append("|")
                    val _day = dayKor.indexOf("${hour[0]}")
                    val _time = Integer.parseInt("${hour[1]}") - 1
                    classHoursBuilder.append(_day)
                            .append(",")
                            .append(_time)
                    if (flag2) {
                        flag2 = false
                        place = tableArray[_time][_day]
                    }
                }
                mEdit.putString(CommonConstants.SUBJECT + count, Subject(title, place, classNumber, teacher, classHoursBuilder.toString(), 0).toString())
                count += 1
            }
        }
        mEdit.putInt(CommonConstants.NUMBER_OF_SUBJECT, count)
        mEdit.putBoolean(CommonConstants.HAS_TIMETABLE, true)
        mEdit.putBoolean(CommonConstants.HAS_CHANGED_DATA, true)
        mEdit.commit()
        context.mProgressDialog.dismiss()
        context.finish()
    }

    fun fillClassroomArray() {
        var flag = true
        for (row in table.getElementsByTag("tr")) {
            if (flag) flag = false
            else {
                var flag2 = true
                val thisRow = ArrayList<String>(0)
                for (rowElement in row.getElementsByTag("td")) {
                    if (flag2) flag2 = false
                    else {
                        val infoArray = rowElement.text().split(" ")
                        thisRow.add(infoArray[infoArray.size - 1])
                    }
                }
                tableArray.add(thisRow)
            }
        }
    }
}