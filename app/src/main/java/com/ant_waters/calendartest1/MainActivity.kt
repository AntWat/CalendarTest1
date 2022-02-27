package com.ant_waters.calendartest1

import android.Manifest
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ant_waters.covidstatistics.Utils.Utils

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    lateinit var _requestPermissionLauncher: ActivityResultLauncher<String?>

    var _onPermissionRequestComplete: OnPermissionRequestComplete = OnPermissionRequestComplete.DoNothing

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // -------------- Initialise the UI
            getWidgets()

            btn__GetCalendars.setOnClickListener { btnGetCalendars_Click() }
            btn__AddEvents.setOnClickListener { btnAddEvents_Click() }

            // --------------  Register the permissions callback
            // which handles the user's response to the system permissions dialog.
            // This must be done before the activity is started.
            _requestPermissionLauncher =
                this.registerForActivityResult(ActivityResultContracts.RequestPermission())
                    { _onPermissionRequestComplete.DoAction(it)}

        }
        catch (ex:Exception)
        {
            throw ex
        }
    }

    // --------------------------------------------

    lateinit var btn__GetCalendars: Button
    lateinit var btn__AddEvents: Button


    fun getWidgets()
    {
        btn__GetCalendars = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__GetCalendars) as Button
        btn__AddEvents = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__AddEvents) as Button
    }

    // --------------------------------------------
    fun btnGetCalendars_Click() {
        try {
            Log.i(MainViewModel.LOG_TAG, "btnGetCalendars_Click: Started")

            // Note that the line below must set the variable used above in the definition of _requestPermissionLauncher.
            // This variable is also passed to PermissionsManager.RequestPermission to be called if the permission is already known.

            _onPermissionRequestComplete = OnPermissionRequestComplete(Manifest.permission.READ_CALENDAR,
                fun (isGranted:Boolean) { GetCalendarsContinue(isGranted) })

            PermissionsManager.RequestPermission(Manifest.permission.READ_CALENDAR,
                this, this, _requestPermissionLauncher,
                _onPermissionRequestComplete
            )

        }
        catch (ex:Exception)
        {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
            Utils.LogAndShowError(ex, this)
        }
    }

    fun GetCalendarsContinue(isGranted_READ_CALENDAR:Boolean) {
        try {
            if (!isGranted_READ_CALENDAR) {
                Utils.LogAndShowError("READ_CALENDAR was denied", this)
                return
            }

            val calendarInfo = CalendarManager.GetCalendars(contentResolver, this)

            if (calendarInfo==null || calendarInfo.count()==0) {
                Utils.ShowToast(this,
                    "No calendars returned",
                    Toast.LENGTH_LONG, -1, Color.RED)
            } else {
                var msg = ""
                for (ci in calendarInfo) { msg += "$ci /r/n" }
                Utils.ShowMessage("Calendars found", msg, this)
            }
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
        finally {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
        }
    }

    // --------------------------------------------
    fun btnAddEvents_Click() {
        try {
            Log.i(MainViewModel.LOG_TAG, "btnAddEvents_Click: Started")

            // Note that the line below must set the variable used above in the definition of _requestPermissionLauncher.
            // This variable is also passed to PermissionsManager.RequestPermission to be called if the permission is already known.

            _onPermissionRequestComplete = OnPermissionRequestComplete(Manifest.permission.WRITE_CALENDAR,
                fun (isGranted:Boolean) { AddEventsContinue(isGranted) })

            PermissionsManager.RequestPermission(Manifest.permission.WRITE_CALENDAR,
                this, this, _requestPermissionLauncher,
                _onPermissionRequestComplete
            )

        }
        catch (ex:Exception)
        {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
            Utils.LogAndShowError(ex, this)
        }
    }

    fun AddEventsContinue(isGranted_WRITE_CALENDAR:Boolean) {
        try {
            if (!isGranted_WRITE_CALENDAR) {
                Utils.LogAndShowError("WRITE_CALENDAR was denied", this)
                return
            }

            val eventIds = mutableListOf<Long>()

            val errMsg = CalendarManager.WriteEvents(contentResolver, eventIds)

            if (errMsg.length>0) { Utils.LogAndShowError(errMsg, this) }
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
        finally {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
        }
    }

    // ---------------------------------------

}