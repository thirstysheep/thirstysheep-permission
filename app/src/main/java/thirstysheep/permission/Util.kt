package thirstysheep.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.example.wky09.requestpermission.Config.PERMISSIONS
import com.example.wky09.requestpermission.Config.PERMS_APPLY_CODE

import java.util.ArrayList

import io.reactivex.disposables.Disposable




object Util : ActivityCompat(){
    fun isPermissionOk(grantResults: IntArray): Boolean {
        var isOk = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //含有未申请到的权限
                isOk = false
                break
            }
        }
        return isOk
    }

    fun isPermitted(context: Context, isApplyPerm: Boolean): Boolean{
        var isNeedApplyPerm = false
        val unApplyedList = ArrayList<String>()
        for (i in 0 until PERMISSIONS.size) {
            if (ContextCompat.checkSelfPermission(context, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                unApplyedList.add(PERMISSIONS[i])   //申请过的权限将其删除，不要再提示给用户
            }
        }
        if (unApplyedList.size > 0) { //有未申请的权限，去申请
            isNeedApplyPerm = true
        }
        return isNeedApplyPerm
    }

    //动态申请权限（Android6.0以上需要如此）
    fun isPermissionsApply(context: Context, isApplyPerm: Boolean): Boolean {
        var isNeedApplyPerm = false
        //判断剩余还未申请的权限
        val unApplyedList = ArrayList<String>()
        for (i in 0 until PERMISSIONS.size) {
            if (ContextCompat.checkSelfPermission(context, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                unApplyedList.add(PERMISSIONS[i])   //申请过的权限将其删除，不要再提示给用户
            }
        }
        if (unApplyedList.size > 0) { //有未申请的权限，去申请
            isNeedApplyPerm = true
            if (isApplyPerm) {
                val unApplyedArray:Array<String> = unApplyedList.toTypedArray()
                ActivityCompat.requestPermissions(context as Activity, unApplyedArray,PERMS_APPLY_CODE)
            }
        }

        return isNeedApplyPerm
    }

    //解注册观察者模式
    fun disposeSubscribe(vararg disposables: Disposable) {
        for (disposable in disposables) {
            if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
            }
        }
    }
}
