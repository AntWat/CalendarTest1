package com.ant_waters.covidstatistics.Utils

import android.content.Context
import android.graphics.Color
import android.text.util.Linkify
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import com.ant_waters.calendartest1.MainViewModel

class Utils() {
    companion object {
        fun ShowToast(context: Context?, msg: String, length: Int = Toast.LENGTH_SHORT,
                      gravity: Int = -1, textColor: Int = Color.BLACK ) {

            val toast = Toast.makeText(context, msg, length)
            if(gravity >= 0) { toast.setGravity(gravity, 0, 0) }
            val view = toast.view

            val text: TextView = view!!.findViewById(android.R.id.message)
            text.setTextColor(textColor)
            toast.show();
        }

        fun ShowMessage(title: String, msg: String, context: Context) {
            try {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(title)
                builder.setMessage(msg)

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                }

                var dlg = builder.create()
                dlg.show()
                Linkify.addLinks((dlg.findViewById(android.R.id.message) as TextView?)!!, Linkify.ALL)
            } catch (ex:Exception) { LogAndShowError(ex, context)}
        }

        fun AskQuestion(title: String, msg: String, context: Context,
                                onYes: (()->Unit)?, onNo: (()->Unit)?) {
            try {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(title)
                builder.setMessage(msg)

                builder.setPositiveButton(android.R.string.yes)
                        { dialog, which-> if (onYes!=null) onYes() }
                builder.setNegativeButton(android.R.string.no)
                        { dialog, which->if (onNo!=null) onNo() }

                var dlg = builder.create()
                dlg.show()
                Linkify.addLinks((dlg.findViewById(android.R.id.message) as TextView?)!!, Linkify.ALL)
            } catch (ex:Exception) { LogAndShowError(ex, context)}
        }

        // ---------------------------------------

        fun ShowError(errMsg: String, context: Context) {
            Utils.ShowToast(context,
                "Error! ${errMsg}",
                Toast.LENGTH_LONG, Gravity.TOP, Color.RED)
        }

        fun LogAndShowError(ex:Exception, context: Context)
        {
            LogAndShowError(ex.message.toString(), context)
        }

        fun LogAndShowError(errMsg:String, context: Context)
        {
            Log.e(MainViewModel.LOG_TAG, errMsg)
            ShowError(errMsg, context)
        }

    }
}