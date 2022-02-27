package com.ant_waters.calendartest1

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL
import android.util.Log

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
        public fun GetCalendars(contentResolver: ContentResolver, context: Context) : List<String>? {
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: GetCalendars")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.READ_CALENDAR)) {
                    throw Exception("READ_CALENDAR permission should have been obtained before calling GetCalendars")
                }

                // Run query
                val uri: Uri = CalendarContract.Calendars.CONTENT_URI
                val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                        "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                        "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
                val selectionArgs: Array<String> =
                    arrayOf("watdev64@gmail.com", "com.google", "watdev64@gmail.com")
                val cur: Cursor? =
                    contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)
//                val cur: Cursor? =
//                    contentResolver.query(uri, EVENT_PROJECTION, null, null, null)

                if (cur == null) { throw Exception("null cursor returned in GetCalendars") }

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

                return calendarInfo

            } catch (ex: Exception) {
                Log.e(MainViewModel.LOG_TAG, "GetCalendars Error: ${ex.message}")
                throw ex
            }
        }
    }
}