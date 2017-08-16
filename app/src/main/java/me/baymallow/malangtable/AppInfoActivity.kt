package me.baymallow.malangtable

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_app_info.*

class AppInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        about_github.setOnClickListener({
            startActivity(Intent(Intent.ACTION_VIEW
                    , Uri.parse("https://github.com/BayMinimum/MalangTable"))
            )
        })

    }
}
