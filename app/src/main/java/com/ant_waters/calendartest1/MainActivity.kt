package com.ant_waters.calendartest1

import android.Manifest
import android.content.ContentResolver
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ant_waters.calendartest1.Utils.Utils

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    // ------------------ For requesting permissions for the App
    lateinit var _requestPermissionLauncher: ActivityResultLauncher<String?>

    // Set this to the required callback just before calling PermissionsManager.RequestPermission:
    var _onPermissionRequestComplete: OnPermissionRequestComplete = OnPermissionRequestComplete.DoNothing


    // ------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // -------------- Initialise the UI
            getWidgets()

            btn__GetCalendars.setOnClickListener { btnGetCalendars_Click() }
            btn__AddEvents.setOnClickListener { btnAddEvents_Click() }
            btn__GetEvents.setOnClickListener { btnGetEvents_Click() }
            btn__DeleteEvents.setOnClickListener { btnDeleteEvents_Click() }

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
    lateinit var btn__GetEvents: Button
    lateinit var btn__DeleteEvents: Button

    fun getWidgets()
    {
        btn__GetCalendars = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__GetCalendars) as Button
        btn__AddEvents = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__AddEvents) as Button
        btn__GetEvents = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__GetEvents) as Button
        btn__DeleteEvents = this.findViewById<View>(com.ant_waters.calendartest1.R.id.btn__DeleteEvents) as Button
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

            val calendarInfo = CalendarManager.GetCalendars(contentResolver)

            if (calendarInfo==null || calendarInfo.count()==0) {
                Utils.ShowToast(this,
                    "No calendars returned",
                    Toast.LENGTH_LONG, -1, Color.RED)
            } else {
                var msg = ""
                for (ci in calendarInfo) { msg += "$ci \n" }
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
    val calID: Long = 1     // TODO

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

            var bContinue: Boolean = false
            Utils.AskQuestion("Add events to calendar?",
                "This is an experimental feature that will add events to the calendar, " +
                        "but could seriously mess it up! " +
                        "You are strongly advised NOT to run it on a real (non-DEV) phone! " +
                        "\n\nDo you want to continue?",
                this,
                fun () { AddEventsContinue2() }, null
            )
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
        finally {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
        }
    }

    fun AddEventsContinue2() {
        try {
            val eventIds = mutableListOf<Long>()
            val errMsg = WriteEvents(contentResolver, calID, eventIds)

            if (errMsg.length>0) { Utils.LogAndShowError(errMsg, this) }
            else { Utils.ShowMessage("AddEvents", "AddEvents succeeded", this) }
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
        finally {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
        }
    }

    fun WriteEvents(contentResolver: ContentResolver, calID: Long,
                           eventIds: MutableList<Long>): String /* errMsg */ {
        var errMsg = ""
        var errMsg2 = ""
        try {
            if  (!PermissionsManager.CheckPermission(Manifest.permission.WRITE_CALENDAR)) {
                throw Exception("WRITE_CALENDAR permission should have been obtained before calling WriteEvents")
            }

            errMsg2 = CalendarManager.WriteEvent(
                contentResolver, calID, eventIds,
                "My2 Saxophone practice", "Enjoy!", "Home",
                "2022.02.28 13:00", 60
            )
            if (errMsg2.length>0) { errMsg += "\n" + errMsg2 }

            errMsg2 = CalendarManager.WriteEvent(
                contentResolver, calID, eventIds,
                "My2 Choir", "Sing well!", "Bowdon Rugby",
                "2022.02.28 17:00", 120
            )
            if (errMsg2.length>0) { errMsg += "\n" + errMsg2 }

            return errMsg

        } catch (ex: Exception) {
            Log.e(MainViewModel.LOG_TAG, "WriteEvents Error: ${ex.message}")
            throw ex
        }
    }


    // --------------------------------------------
    fun btnGetEvents_Click() {
        try {
            Log.i(MainViewModel.LOG_TAG, "btnGetEvents_Click: Started")

            // Note that the line below must set the variable used above in the definition of _requestPermissionLauncher.
            // This variable is also passed to PermissionsManager.RequestPermission to be called if the permission is already known.

            _onPermissionRequestComplete = OnPermissionRequestComplete(Manifest.permission.READ_CALENDAR,
                fun (isGranted:Boolean) { GetEventsContinue(isGranted) })

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

    fun GetEventsContinue(isGranted_READ_CALENDAR:Boolean) {
        try {
            if (!isGranted_READ_CALENDAR) {
                Utils.LogAndShowError("READ_CALENDAR was denied", this)
                return
            }

            val eventIds = mutableListOf<Long>()
            val eventInfo = mutableListOf<String>()
            val errMsg = CalendarManager.GetEvents(contentResolver,
                "2021.09.01 09:00", "2022.07.25 17:00",     // TODO
                eventIds, eventInfo)

            if (errMsg.length>0) {
                Utils.LogAndShowError(errMsg, this)
                return
            }

            if (eventIds==null || eventIds.count()==0) {
                Utils.ShowToast(this,
                    "No events returned",
                    Toast.LENGTH_LONG, -1, Color.RED)
            } else {
                var msg = "${eventInfo.count()} events were found\n"
                for (ei in eventInfo) { msg += "$ei \n" }
                Utils.ShowMessage("Events found", msg, this)
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
    fun btnDeleteEvents_Click() {
        try {
            Log.i(MainViewModel.LOG_TAG, "btnDeleteEvents_Click: Started")

            // Note that the line below must set the variable used above in the definition of _requestPermissionLauncher.
            // This variable is also passed to PermissionsManager.RequestPermission to be called if the permission is already known.

            _onPermissionRequestComplete = OnPermissionRequestComplete(Manifest.permission.WRITE_CALENDAR,
                fun (isGranted:Boolean) { DeleteEventsContinue(isGranted) })

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

    fun DeleteEventsContinue(isGranted_WRITE_CALENDAR:Boolean) {
        try {
            if (!isGranted_WRITE_CALENDAR) {
                Utils.LogAndShowError("WRITE_CALENDAR was denied", this)
                return
            }

            var bContinue: Boolean = false
            Utils.AskQuestion("Delete our events from calendar?",
                "This is an experimental feature that will delete events that we added to the calendar, " +
                        "but could seriously mess it up! " +
                        "You are strongly advised NOT to run it on a real (non-DEV) phone! " +
                        "\n\nDo you want to continue?",
                this,
                fun () { DeleteEventsContinue2() }, null
            )
        }
        catch (ex:Exception)
        {
            Utils.LogAndShowError(ex, this)
        }
        finally {
            _onPermissionRequestComplete = OnPermissionRequestComplete.DoNothing
        }
    }

    fun DeleteEventsContinue2() {
        try {
            val eventIds = mutableListOf<Long>()
            val eventInfo = mutableListOf<String>()
            var errMsg = CalendarManager.GetEvents(contentResolver,
                "2021.09.01 09:00", "2022.07.25 17:00",
                eventIds, eventInfo)

            if (errMsg.length>0) {
                Utils.LogAndShowError(errMsg, this)
                return
            }

            errMsg = CalendarManager.DeleteEvents(contentResolver, eventIds)

            if (errMsg.length>0) { Utils.LogAndShowError(errMsg, this) }
            else { Utils.ShowMessage("DeleteEvents", "DeleteEvents succeeded", this) }
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