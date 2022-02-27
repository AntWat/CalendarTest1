package com.ant_waters.calendartest1

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class CalendarManager {

    companion object {
        //val EVENT_OWNER_TAG = "admin@calendartest1.com"      // Tag that will be used to tag all calendar events added and managed by this App
        val EVENT_DESCRIPTION_TAG = "Added by: CalendarTest1"      // Tag that will be used to tag all calendar events added and managed by this App

        public fun GetCalendars(contentResolver: ContentResolver) : List<String>? {
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: GetCalendars")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.READ_CALENDAR)) {
                    throw Exception("READ_CALENDAR permission should have been obtained before calling GetCalendars")
                }

                // ----------------- Projection array.
                // Creating indices for this array instead of doing dynamic lookups improves performance.
                val PROJECTION_ARRAY: Array<String> = arrayOf(
                    CalendarContract.Calendars._ID,                     // 0
                    CalendarContract.Calendars.ACCOUNT_NAME,            // 1
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
                    CalendarContract.Calendars.OWNER_ACCOUNT            // 3
                )

                // The indices for the projection array above.
                val PROJECTION_ID_INDEX: Int = 0
                val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
                val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
                val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

                // ----------------- Run query
                val uri: Uri = CalendarContract.Calendars.CONTENT_URI
                val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                        "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                        "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
                val selectionArgs: Array<String> =
                    arrayOf("watdev64@gmail.com", "com.google", "watdev64@gmail.com")
                val cur: Cursor? =
                    contentResolver.query(uri, PROJECTION_ARRAY, selection, selectionArgs, null)
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

        public fun WriteEvents(contentResolver: ContentResolver, calID: Long,
                               eventIds: MutableList<Long>): String /* errMsg */ {
            var errMsg = ""
            var errMsg2 = ""
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: WriteEvents")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.WRITE_CALENDAR)) {
                    throw Exception("WRITE_CALENDAR permission should have been obtained before calling WriteEvents")
                }

                errMsg2 += WriteEvent(contentResolver, calID, eventIds,
                    "My2 Saxophone practice", "Enjoy!", "Home",
                    "2022.02.28 13:00", 60)
                if (errMsg2.length>0) { errMsg += "\n" + errMsg2 }

                errMsg2 += WriteEvent(contentResolver, calID, eventIds,
                    "My2 Choir", "Sing well!", "Bowdon Rugby",
                    "2022.02.28 17:00", 120)
                if (errMsg2.length>0) { errMsg += "\n" + errMsg2 }

                return errMsg

            } catch (ex: Exception) {
                Log.e(MainViewModel.LOG_TAG, "GetCalendars Error: ${ex.message}")
                throw ex
            }
        }

        fun WriteEvent(contentResolver: ContentResolver, calID: Long, eventIds: MutableList<Long>,
                           title: String, description: String, location: String,
                           startdateTime: String, durationInMinutes: Int): String /* errMsg */ {
            try {
                //val startMillis: Long = getUtcEpochMillisecs("2022.02.27 13:00")
                val startMillis: Long = getUtcEpochMillisecs(startdateTime)
                //val endMillis: Long = getUtcEpochMillisecs("2022.02.27 14:30")
                val duration = "PT${durationInMinutes}M"

                val values = ContentValues().apply {
                    //put(CalendarContract.Events.ORGANIZER, EVENT_OWNER_TAG)
                        // This is very important, as it is the only way I have found to label the new events,
                        // so we can modify or delete them later

                    put(CalendarContract.Events.DTSTART, startMillis)
                    //put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.DURATION, duration)
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.DESCRIPTION, description + "\n\n$EVENT_DESCRIPTION_TAG\n")
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

        fun getUtcEpochMillisecs(dateTimeString: String): Long {
            val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val date: Date = formatter.parse(dateTimeString)

            // get epoch millis
            val epochMillis: Long = date.getTime()

            return epochMillis
        }

//         The method below cannot be used, as it causes the error:
//         java.lang.IllegalArgumentException: Only sync adapters may write using content://com.android.calendar/extendedproperties
//
//                              val EVENT_PROPERTY_ADDED_BY = "EventAddedBy"
//
//
//                            fun AddCustomProperties(contentResolver: ContentResolver, eventId: Long,
//                                           properties: List<Pair</* PropertyName */ String, /* PropertyValue */ String>>)
//                                                : String /* errMsg */ {
//                                try {
//                                    for (pair in properties) {
//                                        val values = ContentValues().apply {
//                                            put(CalendarContract.ExtendedProperties.EVENT_ID, eventId)
//                                            put(CalendarContract.ExtendedProperties.NAME, pair.first)
//                                            put(CalendarContract.ExtendedProperties.VALUE, pair.second)
//                                        }
//                                        val extendedPropertiesUri: Uri = CalendarContract.ExtendedProperties.CONTENT_URI
//                                        val newUri: Uri? = contentResolver.insert(extendedPropertiesUri, values)
//
//                                        var dumY = 1
//                                    }
//                                    return ""
//                                }
//                                catch (ex:Exception)
//                                {
//                                    return ex.message.toString()
//                                }
//                            }

        // --------------------------------------------

        /// Get all the events that were added by this app
        public fun GetEvents(contentResolver: ContentResolver,
                             startdateTime: String, enddateTime: String,
                             eventIds: MutableList<Long>, eventInfo: MutableList<String>): String /* errMsg */ {
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: GetEvents")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.READ_CALENDAR)) {
                    throw Exception("READ_CALENDAR permission should have been obtained before calling GetEvents")
                }

                // -----------------
                val startMillis: Long = getUtcEpochMillisecs(startdateTime)
                val endMillis: Long = getUtcEpochMillisecs(enddateTime)

                // ----------------- Projection array.
                val PROJECTION_ARRAY: Array<String> = arrayOf(
                    CalendarContract.Instances.EVENT_ID, // 0
                    CalendarContract.Instances.BEGIN, // 1
                    CalendarContract.Instances.TITLE, // 2
                    CalendarContract.Instances.ORGANIZER // 3
                )
                //CalendarContract.Instances.EVENT_ID, // 0

                // The indices for the projection array above.
                val PROJECTION_ID_INDEX: Int = 0
                val PROJECTION_BEGIN_INDEX: Int = 1
                val PROJECTION_TITLE_INDEX: Int = 2
                val PROJECTION_TITLE_ORGANIZER: Int = 3

                // ----------------- Run query
                //val uri: Uri = CalendarContract.Instances.CONTENT_URI
                val selection: String = "(${CalendarContract.Instances.DESCRIPTION} LIKE '%${EVENT_DESCRIPTION_TAG}%')"
                //val selectionArgs: Array<String> = arrayOf("")

                // Construct the query with the desired date range.
                val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder, startMillis)
                ContentUris.appendId(builder, endMillis)

                // Submit the query
                val cur: Cursor? = contentResolver.query(builder.build(),
                    PROJECTION_ARRAY, selection, null, null
                )
                if (cur == null) { throw Exception("null cursor returned in GetEvents") }

                // Use the cursor to step through the returned records
                while (cur.moveToNext()) {
                    // Get the field values
                    val eventID: Long = cur.getLong(PROJECTION_ID_INDEX)
                    val startdateTime: String = cur.getString(PROJECTION_BEGIN_INDEX)
                    val title: String = cur.getString(PROJECTION_TITLE_INDEX)
                    val organizer: String = cur.getString(PROJECTION_TITLE_ORGANIZER)

                    eventIds.add(eventID)
                    eventInfo.add("eventID: $eventID, title: $title, organizer: $organizer, startdateTime: $startdateTime")
                }

                return ""

            } catch (ex: Exception) {
                Log.e(MainViewModel.LOG_TAG, "GetEvents Error: ${ex.message}")
                throw ex
            }
        }




    }
}