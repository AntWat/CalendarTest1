package com.ant_waters.calendartest1

import android.Manifest
import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class CalendarManager {

    companion object {
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
                    arrayOf("watdev64@gmail.com", "com.google", "watdev64@gmail.com")       // TODO
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

        public fun WriteEvent(contentResolver: ContentResolver, calID: Long, eventIds: MutableList<Long>,
                           title: String, description: String, location: String,
                           startdateTime: String, durationInMinutes: Int): String /* errMsg */ {
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: WriteEvent")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.WRITE_CALENDAR)) {
                    throw Exception("WRITE_CALENDAR permission should have been obtained before calling WriteEvent")
                }

                //val startMillis: Long = getUtcEpochMillisecs("2022.02.27 13:00")
                val startMillis: Long = dateTimeStringToUtcEpochMillisecs(startdateTime)
                val duration = "PT${durationInMinutes}M"

                val values = ContentValues().apply {

                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DURATION, duration)
                    put(CalendarContract.Events.TITLE, title)

                    put(CalendarContract.Events.CALENDAR_ID, calID)
                    put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/London")

                    put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                    put(CalendarContract.Events.EVENT_LOCATION, location)
                    put(CalendarContract.Events.HAS_ALARM, 0)
                    //put(CalendarContract.Events.HAS_EXTENDED_PROPERTIES, true)
                    put(CalendarContract.Events.IS_ORGANIZER, 0)

                    put(CalendarContract.Events.DESCRIPTION, description
                            + "\n\n$EVENT_DESCRIPTION_TAG\n")
//                         Note: This is very important, as it is the only way I have found to label the new events,
//                         so we can modify or delete them later.
//
//                         Other methods I tried:
//                              (*) val EVENT_OWNER_TAG = "admin@calendartest1.com"
//                                  put(CalendarContract.Events.ORGANIZER, EVENT_OWNER_TAG)
//                                  -> Failed because the ORGANIZER of an event would get reset by the calendar to its owner.
//                                  I guess this is because it somehow knows that "admin@calendartest1.com" is not a real email address?
//                              (*) Using extendedproperties
//                                  -> Failed with the Exception:
//                                     java.lang.IllegalArgumentException: Only sync adapters may write using content://com.android.calendar/extendedproperties

                }
                val eventsUri: Uri = CalendarContract.Events.CONTENT_URI
                val newUri: Uri? = contentResolver.insert(eventsUri, values)

                // get the event ID that is the last element in the Uri
                val eventID: Long? = newUri?.lastPathSegment?.toLong()?:null

                if (eventID != null)
                {
                    eventIds.add(eventID)
                }

                return  ""
            }
            catch (ex:Exception)
            {
                return ex.message.toString()
            }
        }

        public fun forceSync(context: Context) {
            val extras = Bundle()
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            val account = Account("watdev64@gmail.com", "com.google")   // TODO
            ContentResolver.requestSync(account, CalendarContract.AUTHORITY, extras)
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
                val startMillis: Long = dateTimeStringToUtcEpochMillisecs(startdateTime)
                val endMillis: Long = dateTimeStringToUtcEpochMillisecs(enddateTime)

                // ----------------- Projection array.
                val PROJECTION_ARRAY: Array<String> = arrayOf(
                    CalendarContract.Instances.EVENT_ID, // 0
                    CalendarContract.Instances.BEGIN, // 1
                    CalendarContract.Instances.TITLE, // 2
                    CalendarContract.Instances.ORGANIZER // 3
                )

                // The indices for the projection array above.
                val PROJECTION_ID_INDEX: Int = 0
                val PROJECTION_BEGIN_INDEX: Int = 1
                val PROJECTION_TITLE_INDEX: Int = 2
                val PROJECTION_TITLE_ORGANIZER: Int = 3

                // ----------------- Run query
                val selection: String = "(${CalendarContract.Instances.DESCRIPTION} LIKE '%${EVENT_DESCRIPTION_TAG}%')"

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
                    val startMillisecsString: String = cur.getString(PROJECTION_BEGIN_INDEX)
                    val startdateTime = utcEpochMillisecsToDateTimeString(startMillisecsString.toLong())

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

        // --------------------------------------------

        public fun DeleteEvents(contentResolver: ContentResolver, eventIds: List<Long>): String /* errMsg */ {
            var errMsg = ""
            var errMsg2 = ""
            try {
                Log.i(MainViewModel.LOG_TAG, "Starting: DeleteEvents")

                if  (!PermissionsManager.CheckPermission(Manifest.permission.WRITE_CALENDAR)) {
                    throw Exception("WRITE_CALENDAR permission should have been obtained before calling WriteEvents")
                }

                for (eventId in eventIds) {
                    errMsg2 += DeleteEvent(contentResolver, eventId)
                    if (errMsg2.length>0) { errMsg += "\n" + errMsg2 }
                }

                return errMsg

            } catch (ex: Exception) {
                Log.e(MainViewModel.LOG_TAG, "DeleteEvents Error: ${ex.message}")
                throw ex
            }
        }

        fun DeleteEvent(contentResolver: ContentResolver, eventId: Long): String /* errMsg */ {
            try {
                val deleteUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                val rows: Int = contentResolver.delete(deleteUri, null, null)

                if (rows==0) { return "No rows deleted for event $eventId" }

                return  ""
            }
            catch (ex:Exception)
            {
                return ex.message.toString()
            }
        }

        // ----------------------------------
        val dateTimePattern = "yyyy.MM.dd HH:mm"

        fun dateTimeStringToUtcEpochMillisecs(dateTimeString: String): Long {
            val formatter = SimpleDateFormat(dateTimePattern)
            val date: Date = formatter.parse(dateTimeString)

            // get epoch millis
            val epochMillis: Long = date.getTime()

            return epochMillis
        }

        fun utcEpochMillisecsToDateTimeString(epochMillis: Long): String {
            val formatter = SimpleDateFormat(dateTimePattern)
            val date = Date(epochMillis)
            val dateTimeString = formatter.format(date)

            return dateTimeString
        }


    }
}