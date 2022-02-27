package com.ant_waters.calendartest1

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

public class OnPermissionRequestComplete(val permission: String, val _onGrantedOrDenied: (/* isGranted */Boolean)->Unit)
{
    public fun DoAction(isGranted: Boolean) {
        Log.i(MainViewModel.LOG_TAG, "RequestPermission: isGranted=$isGranted")
        PermissionsManager.RecordPermission(permission, isGranted)
        _onGrantedOrDenied(isGranted)
    }

    companion object {
        public val DoNothing: OnPermissionRequestComplete =
            OnPermissionRequestComplete("", fun(isGranted: Boolean) {})
    }
}

class PermissionsManager {

    companion object {

        // ----------------------------------

        // The variables below are just to avoid redundant calls to checkSelfPermission,
        // after it has been asked once. The App itself will remember the user choice, even after
        // it ends, so the user won't be asked more than once per installation.
        // Note: These will be wiped out if the screen is rotated (unless we put them in a ViewModel),
        // but the users still shouldn't be asked again.
        val _permissionsGranted = mutableListOf<String>()
        val _permissionsRefused = mutableListOf<String>()

        public fun RecordPermission(permission: String, isGranted: Boolean) {
            addOrRemove(permission, _permissionsGranted, isGranted)
            addOrRemove(permission, _permissionsRefused, !isGranted)
        }
        private fun addOrRemove(item: String, theList: MutableList<String>, addToList: Boolean) {
            if (addToList && !theList.contains(item)) { theList.add(item) }
            if (!addToList && theList.contains(item)) { theList.remove(item) }
        }

        public fun CheckPermission(permission: String) : Boolean {
            if (_permissionsGranted.contains(permission)) { return true }
            if (_permissionsRefused.contains(permission)) { return false }
            throw  Exception("Permission '$permission' has not yet been requested. LifecycleOwners must request this before they are STARTED.")
        }

        // ----------------------------------

        public fun RequestPermission(permission: String, context: Context,
                                 componentActivity: ComponentActivity,
                                 requestPermissionLauncher: ActivityResultLauncher<String?>,
                                 onPermissionRequestComplete: OnPermissionRequestComplete ) {
            if (_permissionsGranted.contains(permission)) { onPermissionRequestComplete.DoAction(true) }
            if (_permissionsRefused.contains(permission)) { onPermissionRequestComplete.DoAction(false) }

            when {
                ContextCompat.checkSelfPermission(context,permission)

                 == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionRequestComplete.DoAction(true)
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
        }
    }
}