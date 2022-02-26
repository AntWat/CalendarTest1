package com.ant_waters.calendartest1

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Switch


//import android.graphics.Color
//import android.view.Menu
//import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.ant_waters.covidstatistics.Utils.Utils

//import androidx.appcompat.app.AlertDialog
//import com.google.android.material.snackbar.Snackbar
//import com.google.android.material.navigation.NavigationView
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.navigateUp
//import androidx.navigation.ui.setupActionBarWithNavController
//import androidx.navigation.ui.setupWithNavController
//import androidx.drawerlayout.widget.DrawerLayout
//import android.text.util.Linkify
//import android.view.Gravity
//
//import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // -------------- Initialise the UI
        getWidgets()

        btn__GetCalendars.setOnClickListener { btnGetCalendars_Click() }

        this.contentResolver

    }

    lateinit var btn__GetCalendars: Button

    fun getWidgets()
    {
        btn__GetCalendars = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__GetCalendars) as Button
    }

    fun btnGetCalendars_Click() {
        try {
            Log.i(MainViewModel.LOG_TAG, "btnGetCalendars_Click: Started")

            CalendarManager.GetCalendars(contentResolver, this, this, ::GetCalendarsContinue)
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
    }

    fun GetCalendarsContinue(errMsg: String, calendarInfo: List<String>?) {
        if (errMsg.length > 0) {
            Utils.LogAndShowError(errMsg, this)
            return
        }

        if (calendarInfo == null) {
            Utils.ShowToast(this,
                "No calendars returned",
                Toast.LENGTH_LONG, -1, Color.RED)
        } else {
            var msg = ""
            for (ci in calendarInfo) { msg += "$ci /r/n" }
            Utils.ShowMessage("Calendars found", msg, this)
        }
    }

    // ---------------------------------------

}