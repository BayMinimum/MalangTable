package me.baymallow.malangtable

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var rows: ArrayList<Array<Button?>> = ArrayList(0)
    var data: ArrayList<Subject> = ArrayList(0)
    lateinit var mPref: SharedPreferences
    private var FIRST_ONSTART = true
    var CURRENT_COLOR_CODE = 0
    lateinit var mDialogView: View
    lateinit var mHandler: Handler

    val COLORS = arrayOf(R.color.transparent, R.color.red, R.color.pink, R.color.orange, R.color.yellow, R.color.yellow_green, R.color.green, R.color.teal, R.color.light_blue, R.color.blue, R.color.indigo, R.color.purple, R.color.brown, R.color.gray)
    val COLOR_PICKER_ID = arrayOf(R.id.color_picker_transparent, R.id.color_picker_red, R.id.color_picker_pink, R.id.color_picker_orange, R.id.color_picker_yellow, R.id.color_picker_yellow_green, R.id.color_picker_green, R.id.color_picker_teal, R.id.color_picker_light_blue, R.id.color_picker_blue, R.id.color_picker_indigo, R.id.color_picker_purple, R.id.color_picker_brown, R.id.color_picker_gray)
    val COLOR_PICKER_CHECK_ID = arrayOf(R.id.color_picker_check_transparent, R.id.color_picker_check_red, R.id.color_picker_check_pink, R.id.color_picker_check_orange, R.id.color_picker_check_yellow, R.id.color_picker_check_yellow_green, R.id.color_picker_check_green, R.id.color_picker_check_teal, R.id.color_picker_check_light_blue, R.id.color_picker_check_blue, R.id.color_picker_check_indigo, R.id.color_picker_check_purple, R.id.color_picker_check_brown, R.id.color_picker_check_gray)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resolveTimetableViews()

        mPref = getSharedPreferences(packageName + ".pref", Context.MODE_PRIVATE)

        about.setOnClickListener({
            startActivity(Intent(applicationContext, AppInfoActivity::class.java))
        })

        renew_timetable.setOnClickListener({
            gotoDownload()
        })

        val mAdRequest = AdRequest.Builder().build()
        main_adview.loadAd(mAdRequest)

        mHandler = Handler()
    }

    override fun onStart() {
        super.onStart()

        if (!mPref.getBoolean(CommonConstants.HAS_TIMETABLE, false)) {
            // No timetable saved
            // Prompt to get new one
            val mBuilder = AlertDialog.Builder(this)
                    .setMessage(R.string.no_timetable_msg)
                    .setTitle(R.string.no_timetable_title)
                    .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                        gotoDownload()
                    })
                    .setNegativeButton(R.string.later, { _: DialogInterface, _: Int ->
                        Toast.makeText(this, R.string.need_timetable_to_run, Toast.LENGTH_LONG).show()
                        finish()
                    })
                    .setOnCancelListener({
                        Toast.makeText(this, R.string.need_timetable_to_run, Toast.LENGTH_LONG).show()
                        finish()
                    })
            val mDialog = mBuilder.create()
            mDialog.show()
        } else if (mPref.getBoolean(CommonConstants.HAS_CHANGED_DATA, true) or FIRST_ONSTART) {
            FIRST_ONSTART = false
            mPref.edit().putBoolean(CommonConstants.HAS_CHANGED_DATA, false).apply()
            updateTimetable()
        }
    }

    fun updateTimetable() {
        var i = 0
        val N = mPref.getInt(CommonConstants.NUMBER_OF_SUBJECT, 0)
        while (i < N) {
            val thisSubject = Subject.parseString(mPref.getString(CommonConstants.SUBJECT + i, ";;;;;;"))
            val btnText = thisSubject.toBtnString()
            for (classHour in thisSubject.getParsedClassHours()) {
                val classBtn = rows[classHour[1]][classHour[0]]
                classBtn!!.text = btnText
                classBtn.setBackgroundColor(resources.getColor(COLORS[thisSubject.colorCode]))
                val thisI = i
                classBtn.setOnClickListener({
                    onClickRouter(thisI)
                })
            }
            data.add(thisSubject)
            i += 1
        }
    }

    private fun resolveTimetableViews() {
        val rowsArr = arrayOf(
                R.id.period_1,
                R.id.period_2,
                R.id.period_3,
                R.id.period_4,
                R.id.period_5,
                R.id.period_6,
                R.id.period_7,
                R.id.period_8,
                R.id.period_9,
                R.id.period_10,
                R.id.period_11
        )

        val daysArr = arrayOf(R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri)
        var i = 0
        val typeface = Typeface.createFromAsset(assets, "fonts/NotoSansCJK-Medium.ttc")
        rowsArr.map { main_container.findViewById<LinearLayout>(it) }.forEach { thisRow ->
            var j = 0
            val thisRowArr = Array<Button?>(5, { null })
            daysArr.forEach {
                if ((j < 4) or (i < 9)) {
                    val thisBtn = thisRow.findViewById<Button>(it)
                    thisBtn.typeface = typeface
                    thisRowArr[j] = thisBtn
                }
                j += 1
            }
            rows.add(thisRowArr)
            i += 1
        }
    }

    private fun gotoDownload() {
        val i = Intent(applicationContext, DownloadActivity::class.java)
        startActivity(i)
    }

    fun onClickRouter(i: Int) {
        val mBuilder = AlertDialog.Builder(this)
        mDialogView = layoutInflater.inflate(R.layout.dialog_subject_info, null)
        val thisSubject = data[i]
        mDialogView.findViewById<EditText>(R.id.subject_name).setText(thisSubject.title)
        mDialogView.findViewById<TextView>(R.id.subject_teacher_value).text = thisSubject.teacher
        mDialogView.findViewById<TextView>(R.id.subject_class_value).text = Integer.toString(thisSubject.number)
        mDialogView.findViewById<TextView>(R.id.subject_classroom_value).text = thisSubject.place
        CURRENT_COLOR_CODE = thisSubject.colorCode
        mDialogView.findViewById<ImageView>(COLOR_PICKER_CHECK_ID[CURRENT_COLOR_CODE]).visibility = View.VISIBLE
        mBuilder.setView(mDialogView)
                .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                    val subjectNameEdit = mDialogView.findViewById<EditText>(R.id.subject_name).text.toString()
                    var changedFlag = false
                    if (subjectNameEdit != thisSubject.title) {
                        thisSubject.title = subjectNameEdit
                        changedFlag = true
                    }
                    if (CURRENT_COLOR_CODE != thisSubject.colorCode) {
                        thisSubject.colorCode = CURRENT_COLOR_CODE
                        changedFlag = true
                    }
                    if (changedFlag) mHandler.post {
                        mPref.edit().putBoolean(CommonConstants.HAS_CHANGED_DATA, true).putString(CommonConstants.SUBJECT + i, thisSubject.toString()).commit()
                        updateTimetable()
                    }

                })
                .setNegativeButton(R.string.cancel, { _: DialogInterface, _: Int ->
                    // do nothing
                })
        val mDialog = mBuilder.create()
        mDialog.show()
    }

    fun onColorPickerClick(v: View) {
        val NEW_COLOR_CODE = COLOR_PICKER_ID.indexOf(v.id)
        if ((CURRENT_COLOR_CODE == NEW_COLOR_CODE) or (NEW_COLOR_CODE == -1)) return
        mDialogView.findViewById<ImageView>(COLOR_PICKER_CHECK_ID[CURRENT_COLOR_CODE]).visibility = View.INVISIBLE
        mDialogView.findViewById<ImageView>(COLOR_PICKER_CHECK_ID[NEW_COLOR_CODE]).visibility = View.VISIBLE
        CURRENT_COLOR_CODE = NEW_COLOR_CODE
    }

}

class Subject(var title: String, val place: String, val number: Int, val teacher: String, val classHours: String, var colorCode: Int) {
    companion object {
        fun parseString(saved: String): Subject {
            val split = saved.split(";")
            return Subject(split[0], split[1], Integer.parseInt(split[2]), split[3], split[4], Integer.parseInt(split[5]))
        }
    }

    override fun toString(): String {
        return "$title;$place;$number;$teacher;$classHours;$colorCode"
    }

    fun getParsedClassHours(): List<List<Int>> {
        return classHours.split("|").map { it.split(",").map { Integer.parseInt(it) } }
    }

    fun toBtnString(): String {
        return "$title\n$place"
    }
}