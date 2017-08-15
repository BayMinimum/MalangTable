package me.baymallow.malangtable

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    lateinit var container: LinearLayout
    var rows: ArrayList<Array<Button>> = ArrayList(0)
    lateinit var mPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById<LinearLayout>(R.id.main_container)
        resolveTimetableViews()

        mPref = getSharedPreferences(packageName + ".pref", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        if (mPref.getBoolean(CommonConstants.HAS_TIMETABLE, false)) {
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
                    .setOnDismissListener({
                        Toast.makeText(this, R.string.need_timetable_to_run, Toast.LENGTH_LONG).show()
                        finish()
                    })
            val mDialog = mBuilder.create()
            mDialog.show()
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
        rowsArr.map { container.findViewById<Button>(it) }
                .forEach {
                    rows.add(arrayOf(it.findViewById(R.id.mon),
                            it.findViewById(R.id.tue),
                            it.findViewById(R.id.wed),
                            it.findViewById(R.id.thu),
                            it.findViewById(R.id.fri)))
                }
    }

    private fun gotoDownload() {
        val i = Intent(applicationContext, DownloadActivity::class.java)
        startActivity(i)
    }
}
