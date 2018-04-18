package thirstysheep.permission

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView

import com.example.wky09.requestpermission.Config

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit

class PageStartActivity : AppCompatActivity() {
    private var mActivity: Activity? = null
    private var mPermApplyDialog: AlertDialog? = null
    private var mPermSettingDialog: AlertDialog? = null
    private var mShowViewDis: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        createView()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar!!.hide()
    }

    private fun createView(){
        verticalLayout {
            lparams(matchParent, matchParent)
            imageView(R.drawable.page_start){
                scaleType = ImageView.ScaleType.FIT_XY
            }
            val al = alert{
                icon = resources.getDrawable(R.mipmap.ic_launcher)
                title = "温馨提示"
                message = "使用该程序前，需要对手机的相机及储存权限进行申请"
                isCancelable = false
                neutralPressed("我知道了"){
                    initView()
                }
            }
            if(Util.isPermitted(context,true)){
                al.show()
            }else{
                initView()
            }
        }
    }



    private fun initView() {
        mActivity = this
        //动态申请权限
        if (!Util.isPermissionsApply(this, true)) {
            createShowViewSub()
        }
    }

    private fun createShowViewSub() {
        val fl = Flowable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    startActivity(Intent(mActivity, MainActivity::class.java))
                    finish()
                }

        mShowViewDis = fl.subscribe()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERM_SETTING_APPLY_CODE -> if (!Util.isPermissionsApply(this, false)) {     //权限已经全部申请
                if (mPermSettingDialog != null && mPermSettingDialog!!.isShowing()) {
                    mPermSettingDialog!!.dismiss()
                }
                createShowViewSub()
            }
        }
    }

    /**
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            Config.PERMS_APPLY_CODE -> if (!Util.isPermissionOk(grantResults)) {   //有未申请的权限
                var isCanShowApplyAgain = false
                for (i in permissions.indices) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity!!, permissions[i])) {    //只要有一个权限显示可以弹出继续申请，则可以弹出申请权限对话框
                        isCanShowApplyAgain = true
                        break
                    }
                }
                if (isCanShowApplyAgain) {  //可以继续弹出权限申请
                    showPermApplyDialog()
                } else {    //不能够继续弹出
                    showPermSettingDialog()
                }
            } else {
                createShowViewSub()
            }
        }
    }

    /**
     * 有未申请的权限，未选择不再询问，继续弹出权限申请
     */
    private fun showPermApplyDialog() {
        mPermApplyDialog = AlertDialog.Builder(this)
                .setTitle(R.string.perm_apply_title)
                .setMessage(R.string.perm_apply_msg)
                .setPositiveButton(R.string.apply) { dialogInterface, i -> Util.isPermissionsApply(mActivity!!, true) }
                .setNegativeButton(R.string.cancel) { dialogInterface, i -> finish() }
                .setCancelable(false)
                .create()
        mPermApplyDialog!!.show()
    }

    /**
     * 有未申请的权限，并选了不再询问，进入设置设置应用权限
     */
    private fun showPermSettingDialog() {
        mPermSettingDialog = AlertDialog.Builder(this)
                .setTitle(R.string.perm_apply_title)
                .setMessage(R.string.perm_setting_msg)
                .setPositiveButton(R.string.goto_setting, null)
                .setNegativeButton(R.string.cancel) { dialogInterface, i -> finish() }
                .setCancelable(false)
                .create()
        mPermSettingDialog!!.show()
        //以下方式可以让alertDialog点击时不自动消失
        mPermSettingDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val intent = Intent()
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            startActivityForResult(intent, PERM_SETTING_APPLY_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Util.disposeSubscribe(mShowViewDis!!)
    }

    companion object {
        private val PERM_SETTING_APPLY_CODE = 10
    }
}
