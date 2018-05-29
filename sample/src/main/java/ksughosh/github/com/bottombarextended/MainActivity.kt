package ksughosh.github.com.bottombarextended

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBarFunctions()
    }

    private fun bottomBarFunctions() {
        // bottom bar functions
        bottomBar?.updateBadgeText(R.id.bottomBar_one, "this")
        bottomBar?.setOnItemSelect {
            // change page or fragment
            true
        }
        bottomBar?.setOnItemReselect {
            // do on reselect
        }
        bottomBar?.addBadgeDrawable(R.id.bottomBar_three, R.drawable.ic_add_alert_black)
        Log.d("BottomBar", "Current Item Selected ${bottomBar?.selectedItemId}")
    }

}
