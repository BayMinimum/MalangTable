package me.baymallow.malangtable

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var rows: ArrayList<Array<Button>> = ArrayList(0)
    var data: ArrayList<Subject> = ArrayList(0)
    lateinit var mPref: SharedPreferences
    private var FIRST_ONSTART = true

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
            var i = 0
            val N = mPref.getInt(CommonConstants.NUMBER_OF_SUBJECT, 0)
            while (i < N) {
                val thisSubject = Subject.parseString(
                        mPref.getString(CommonConstants.SUBJECT + i, ";;;;;;"))
                val btnText = thisSubject.toBtnString()
                for (classHour in thisSubject.getParsedClassHours()) {
                    val classBtn = rows[classHour[1]][classHour[0]]
                    classBtn.text = btnText
                    assignColor(classBtn, thisSubject.colorCode)
                    classBtn.setOnClickListener({
                        onClickRouter(i)
                    })
                }
                data.add(thisSubject)
                i += 1
            }
        }
    }

    private fun assignColor(btn: Button, colorCode: Int) {
        val colors = CommonConstants.COLORS[colorCode]
        btn.setBackgroundColor(colors[0])
        btn.setTextColor(colors[1])
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
        var i = 0
        rowsArr.map { main_container.findViewById<LinearLayout>(it) }
                .forEach {
                    if (i < 9) rows.add(arrayOf(it.findViewById(R.id.mon),
                            it.findViewById(R.id.tue),
                            it.findViewById(R.id.wed),
                            it.findViewById(R.id.thu),
                            it.findViewById(R.id.fri)))
                    else rows.add(arrayOf(it.findViewById(R.id.mon),
                            it.findViewById(R.id.tue),
                            it.findViewById(R.id.wed),
                            it.findViewById(R.id.thu)))
                    i += 1
                }
    }

    private fun gotoDownload() {
        val i = Intent(applicationContext, DownloadActivity::class.java)
        startActivity(i)
    }

    fun onClickRouter(i: Int) {
        Toast.makeText(this, data[i].toString(), Toast.LENGTH_SHORT).show()
        TODO("Create dialog for class info & color setting")
    }
}

class Subject(val title: String, val place: String, val number: Int, val teacher: String, val classHours: String, val colorCode: Int) {
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