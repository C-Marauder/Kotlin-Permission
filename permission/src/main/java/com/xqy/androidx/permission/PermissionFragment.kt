package com

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

internal class PermissionFragment: Fragment() {
    companion object {
        fun getInstance()= PermissionFragment()


    }

     val permissionMap:HashMap<String, MutableLiveData<Permission>> by lazy {
        hashMapOf<String, MutableLiveData<Permission>>()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.REQUEST_CODE){
            permissions.forEachIndexed { index, s ->
                permissionMap[s]!!.value = Permission(
                    s,
                    grantResults[index] == PackageManager.PERMISSION_GRANTED
                )
            }
        }
    }
}