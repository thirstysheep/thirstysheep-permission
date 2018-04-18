package com.example.wky09.requestpermission

import android.Manifest

object Config {
    //权限code
    val PERMS_APPLY_CODE = 0
    //需要申请的权限列表
    var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
}
