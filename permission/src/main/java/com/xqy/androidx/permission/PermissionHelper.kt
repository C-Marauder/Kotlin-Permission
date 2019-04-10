package com.xqy.androidx.permission

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class PermissionHelper private constructor() {

    private lateinit var mFragment: PermissionFragment
    companion object {
        private const val TAG: String = "PermissionHelper"
        const val REQUEST_CODE: Int = 0x11

        fun from(activity: AppCompatActivity) : PermissionHelper = INSTANCE.apply {
                commitPermissionFragment(activity.supportFragmentManager)
            }

        fun from(fragment: Fragment): PermissionHelper = INSTANCE.apply {
                commitPermissionFragment(fragment.childFragmentManager)
            }
        //线程安全的单例
        private val INSTANCE: PermissionHelper by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED){
            PermissionHelper()
        }
    }
    private fun commitPermissionFragment(fragmentManager: FragmentManager) {
        mFragment = fragmentManager.findFragmentByTag(TAG) as PermissionFragment? ?: PermissionFragment.getInstance()
        if (!mFragment.isAdded) {
            fragmentManager.beginTransaction()
                .add(mFragment, TAG)
                .commitAllowingStateLoss()
        }

    }
    fun checkPermission(vararg permissions: String, hasPermission: (permission: String, isGranted: Boolean) -> Unit) =
        apply {
            permissions.forEachIndexed { _, permission ->
                val isGranted = ContextCompat.checkSelfPermission(
                    mFragment.context!!,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                hasPermission(permission, isGranted)

            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            mFragment.context!!,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun validatePermissions(permissions: Array<out String>) {
        if (permissions.isNullOrEmpty()) throw Exception("the permission can't be null")
    }


    fun requestPermission(vararg permissions: String, observer: (permission: String, isGranted: Boolean) -> Unit) {
        validatePermissions(permissions)
        //筛选出未授权的权限
        val permissionList = permissions.filter { permission -> !checkPermission(permission) }
        if (permissionList.isNotEmpty()) {//申请的权限列表不为空
            val permissionMap = mFragment.permissionMap
            permissionList.forEach { permission ->
                if (permissionMap[permission] == null) {//判断该权限是否已在权限Map中存在
                    permissionMap[permission] = MutableLiveData<Permission>().apply {
                        this.observe(mFragment, Observer {
                            observer(permission, it!!.isGranted)
                        })
                    }
                }

            }
            //请求权限
            mFragment.requestPermissions(permissionList.toTypedArray(),
                REQUEST_CODE
            )

        }


    }

}