package com.ant_waters.calendartest1

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

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
        val CALENDAR_TAG = "CalendarTest1"      // Tag that will be used to tag all calendar events added and managed by this App
        val EVENT_PROPERTY_ADDED_BY = "EventAddedBy"

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

        // --------------------------------------------

        public fun WriteEvents(contentResolver: ContentResolver,
                               eventIds: MutableList<Long>): String /* errMsg */ {
            var errMsg = ""
            var errMsg2 = ""
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: WriteEvents")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.WRITE_CALENDAR)) {
                    throw Exception("WRITE_CALENDAR permission should have been obtained before calling WriteEvents")
                }

                errMsg2 += WriteEvent(contentResolver, eventIds,
                    "My2 Saxophone practice", "Enjoy!", "Home",
                    "2022.02.28 13:00", 60)
                if (errMsg2.length>0) { errMsg += "\r\n" + errMsg2 }

                errMsg2 += WriteEvent(contentResolver, eventIds,
                    "My2 Choir", "Sing well!", "Bowdon Rugby",
                    "2022.02.28 17:00", 120)
                if (errMsg2.length>0) { errMsg += "\r\n" + errMsg2 }

                return errMsg

            } catch (ex: Exception) {
                Log.e(MainViewModel.LOG_TAG, "GetCalendars Error: ${ex.message}")
                throw ex
            }
        }

        fun WriteEvent(contentResolver: ContentResolver, eventIds: MutableList<Long>,
                           title: String, description: String, location: String,
                           startdateTime: String, durationInMinutes: Int): String /* errMsg */ {
            try {
                val calID: Long = 1     // TODO
                //val startMillis: Long = getUtcEpochMillisecs("2022.02.27 13:00")
                val startMillis: Long = getUtcEpochMillisecs(startdateTime)
                //val endMillis: Long = getUtcEpochMillisecs("2022.02.27 14:30")
                val duration = "PT${durationInMinutes}M"

                val values = ContentValues().apply {
                    put(CalendarContract.Events.ORGANIZER, CALENDAR_TAG)
                        // This is very important, as it is the only way I have found to label the new events,
                        // so we can modify or delete them later

                    put(CalendarContract.Events.DTSTART, startMillis)
                    //put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.DURATION, duration)
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.DESCRIPTION, description)
                    put(CalendarContract.Events.CALENDAR_ID, calID)
                    put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/London")

                    put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                    put(CalendarContract.Events.EVENT_LOCATION, location)
                    put(CalendarContract.Events.HAS_ALARM, 0)
                    //put(CalendarContract.Events.HAS_EXTENDED_PROPERTIES, true)
                    put(CalendarContract.Events.IS_ORGANIZER, 0)
                }
                val eventsUri: Uri = CalendarContract.Events.CONTENT_URI
                val newUri: Uri? = contentResolver.insert(eventsUri, values)

                // get the event ID that is the last element in the Uri
                val eventID: Long? = newUri?.lastPathSegment?.toLong()?:null

                if (eventID != null)
                {
                    eventIds.add(eventID)

//                    var errMsg2: String = AddCustomProperties(contentResolver, eventID,
//                                    List(1){ Pair(EVENT_PROPERTY_ADDED_BY, CALENDAR_TAG) })
//                    if (errMsg2.length>0) { return errMsg2 }
                }

                return  ""
            }
            catch (ex:Exception)
            {
                return ex.message.toString()
            }
        }



        // The method below cannot be used, as it causes the error:
        // java.lang.IllegalArgumentException: Only sync adapters may write using content://com.android.calendar/extendedproperties
        fun AddCustomProperties(contentResolver: ContentResolver, eventId: Long,
                       properties: List<Pair</* PropertyName */ String, /* PropertyValue */ String>>)
                            : String /* errMsg */ {
            try {
                for (pair in properties) {
                    val values = ContentValues().apply {
                        put(CalendarContract.ExtendedProperties.EVENT_ID, eventId)
                        put(CalendarContract.ExtendedProperties.NAME, pair.first)
                        put(CalendarContract.ExtendedProperties.VALUE, pair.second)
                    }
                    val extendedPropertiesUri: Uri = CalendarContract.ExtendedProperties.CONTENT_URI
                    val newUri: Uri? = contentResolver.insert(extendedPropertiesUri, values)

                    var dumY = 1
                }
                return ""
            }
            catch (ex:Exception)
            {
                return ex.message.toString()
            }
        }

        fun getUtcEpochMillisecs(dateTimeString: String): Long {
            val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val date: Date = formatter.parse(dateTimeString)

            // get epoch millis
            val epochMillis: Long = date.getTime()

            return epochMillis
        }

    }
}