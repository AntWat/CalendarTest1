package com.ant_waters.calendartest1

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

// Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
private val EVENT_PROJECTION: Array<String> = arrayOf(
    CalendarContract.Calendars._ID,                     // 0
    CalendarContract.Calendars.ACCOUNT_NAME,            // 1
    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
    CalendarContract.Calendars.OWNER_ACCOUNT            // 3
)

// The indices for the projection array above.
private const val PROJECTION_ID_INDEX: Int = 0
private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

class CalendarManager {

    companion object {
        public fun GetCalendars(contentResolver: ContentResolver, context: Context,
                componentActivity: ComponentActivity,
                onCalendarsRead: (/* errMsg */ String, /* Calendar Info */ List<String>?)->Unit)  {

            GetPermission(
                "READ_CALENDAR", context,
                componentActivity,
                fun(isGranted_READ_CALENDAR: Boolean) {
                    GetCalendarsContinue(isGranted_READ_CALENDAR, contentResolver, onCalendarsRead) }
                )
        }

        fun GetCalendarsContinue(isGranted_READ_CALENDAR: Boolean,
                                 contentResolver: ContentResolver,
                                 onCalendarsRead: (/* errMsg */ String, /* Calendar Info */ List<String>?)->Unit)  {
            try {
                // TODO: Log.e(MainViewModel.LOG_TAG, "InsertToDatabase Error: ${ex.message}")

                if (!isGranted_READ_CALENDAR) {
                    onCalendarsRead("READ_CALENDAR denied", null)
                    return
                }

                // Run query
                val uri: Uri = CalendarContract.Calendars.CONTENT_URI
                val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                        "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                        "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
                val selectionArgs: Array<String> =
                    arrayOf("hera@example.com", "com.example", "hera@example.com")
                val cur: Cursor? =
                    contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)

                if (cur == null) {
                    onCalendarsRead("", null)       // TODO: Error?
                    return
                }

                val calendarInfo = mutableListOf<String>()
                // Use the cursor to step through the returned records
                while (cur.moveToNext()) {
                    // Get the field values
                    val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
                    val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                    val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                    val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)

                    calendarInfo.add("calID: $calID, displayName: $displayName, accountName: $accountName, ownerName: $ownerName")
                }

                onCalendarsRead("", calendarInfo)

            } catch (ex: Exception) {
                // TODO: Log.e(MainViewModel.LOG_TAG, "InsertToDatabase Error: ${ex.message}")
                onCalendarsRead(ex.message.toString(), null)
            }
        }


        // TODO: Check the lines below:
        // I think the variables below are just to avoid redundant calls to checkSelfPermission
        // after it has been asked once.  I think the App itself will remember the user choice, even after
        // it ends, so the user won't be asked more than once per installation.
        // Note: These will be wiped out if the screen is rotated (unless we put them in a ViewModel),
        // but the users still shouldn't be asked again.
        val _permissionsGranted = mutableListOf<String>()
        val _permissionsRefused = mutableListOf<String>()


        public fun GetPermission(permission: String, context: Context,
                                 componentActivity: ComponentActivity,
                                 onGrantedOrDenied: (/* isGranted */Boolean)->Unit ) {
            if (_permissionsGranted.contains(permission)) { onGrantedOrDenied(true) }
            if (_permissionsRefused.contains(permission)) { onGrantedOrDenied(false) }

            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog. Save the return value, an instance of
            // ActivityResultLauncher. You can use either a val, as shown in this snippet,
            // or a lateinit var in your onAttach() or onCreate() method.
            val requestPermissionLauncher =
                componentActivity.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    val isGranted = it
                    if (isGranted) { _permissionsGranted.add(permission) }
                    else { _permissionsRefused.add(permission) }
                    onGrantedOrDenied(isGranted) }

//                    isGranted: Boolean ->
//                    if (isGranted) {
//                        // Permission is granted. Continue the action or workflow in your
//                        // app.
//                    } else {
//                        // Explain to the user that the feature is unavailable because the
//                        // features requires a permission that the user has denied. At the
//                        // same time, respect the user's decision. Don't link to system
//                        // settings in an effort to convince the user to change their
//                        // decision.
//                    }
//                }



            when {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                }
//                shouldShowRequestPermissionRationale(...) -> {
//                // In an educational UI, explain to the user why your app requires this
//                // permission for a specific feature to behave as expected. In this UI,
//                // include a "cancel" or "no thanks" button that allows the user to
//                // continue using your app without granting the permission.
//                showInContextUI(...)            }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        permission)
                }
            }

//
//            // TODO: if  (shouldShowRequestPermissionRationale())
//
//            val sCheck = context.checkSelfPermission(Manifest.permission)
//            val bCheck = sCheck == PERMISSION_GRANTED
//            if (bCheck) {
//                _permissionsGranted.add(Manifest.permission)
//            }
//            return bCheck
        }
    }
}