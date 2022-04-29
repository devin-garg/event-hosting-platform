package live.streamself.streamselflive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_post_tip_feedback.*
import live.streamself.streamselflive.R
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity(), LoginElem.LoginElemCallback, livecontent.livecontentCallback, ViewModelMainAct.LiveCallback {
    private lateinit var instructionAdapter: InstructionAdapter
    private var authvc: LoginElem = LoginElem()
    private var livevc: livecontent = livecontent()
    enum class DarkModeConfig{
        YES,
        NO
    }
    private lateinit var mviewModel: ViewModelMainAct
    private val mAuthMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // Get extra data included in the Intent
//            val message = intent.getStringExtra("message")
            runOnUiThread {
                refreshAuth(true)
                setLiveContent()
            }
        }
    }
    private val mAuthPostTip: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // Get extra data included in the Intent
//            val message = intent.getStringExtra("message")
            val title = p1?.getStringExtra("title")
            val username = p1?.getStringExtra("username")
            val liveid = p1?.getStringExtra("liveid")
            val timedate = p1?.getLongExtra("timedate", 0.toLong())
            val duration = p1?.getIntExtra("duration", 0)
            if (title != null && username != null && liveid != null && timedate != null && duration != null && timedate!! > 0.toLong() && duration!! > 0){
                runOnUiThread {
                    val mc = getPostTipStorages()
                    var containsIt = false
                    for (m in mc){
                        if (m.liveid == liveid!!){
                            containsIt = true
                        }
                    }
                    if (!containsIt){
                        val pt = PostTipStore(title!!, username!!, liveid!!, timedate!!, duration!!)
                        mc.add(pt)
                        savePostTipStorages(mc)
                    }
                }
            }

        }
    }
    private fun savePostTipStorages(ct: ArrayList<PostTipStore>){
        val apt = AllPostTips(ct)
        val sharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        try {
            val json = gson.toJson(apt)
            if (json != null){
                with(sharedPreferences.edit()) {
                    putString(live.streamself.streamselflive.Global.LocalStore.getPostTipStore(), json)
                    apply()
                }
            }

        } catch (ex: Exception) {
        }
    }
    private fun getPostTipStorages(): ArrayList<PostTipStore>{
        val sharedPreferences = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(Global.LocalStore.getPostTipStore(), "")
        var myPostTips = ArrayList<PostTipStore>()
        if (json != null) {
            try {
                val obj = gson.fromJson<AllPostTips>(json!!, AllPostTips::class.java)
                if (obj != null) {
                    myPostTips = obj!!.content
                }
            } catch (ex: Exception) {

            }

        }
        return myPostTips
    }
    private fun checkIfCanShowPostTip(){
        var myStore = getPostTipStorages()
        var i = 0
        var j: Int? = null
        for (ms in myStore) {
           if (Date() >= Date((ms.timedate + ms.durationval*60000 - 3000).toLong())) {
               j = i
               break
           }
            i += 1
        }
        if (j != null) {
            val mc = myStore.removeAt(j!!)
            val myIntent = Intent(this, PostTipFeedback::class.java)
            myIntent.putExtra("pdtitle", mc.title)
            myIntent.putExtra("pdusername", mc.author)
            myIntent.putExtra("pdliveid", mc.liveid)
            this.startActivity(myIntent)
            savePostTipStorages(myStore)
        }
    }
    private val mAuthFinished: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // Get extra data included in the Intent
//            val message = intent.getStringExtra("message")
            runOnUiThread {
                presentSuccErrMsg("Your LIVE has ended! Great to see you hosting!", true)
            }
        }
    }
    private val mAuthErr: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // Get extra data included in the Intent
//            val message = intent.getStringExtra("message")
            val err = p1?.getIntExtra("err", -1)
            if (err != null && err != -1) {
                runOnUiThread {
                    presentSuccErrMsg(err!!, false)
                }
            }
        }
    }
    private fun presentSuccErrMsg(s: Int, isGood: Boolean?) {
        val toast = Toast.makeText(
            applicationContext,
            s,
            Toast.LENGTH_LONG
        )
        val toastView = toast.view
        toastView.setBackgroundResource(R.color.colorInvBackgroundDef)
        val v =
            toast.view.findViewById<View>(android.R.id.message) as TextView
        if (isGood == null){
            v.setTextColor(Color.parseColor("#FFA500"))
        } else {
            if (isGood!!) {
                v.setTextColor(Color.GREEN)
            } else {
                v.setTextColor(Color.RED)
            }
        }
        toast.show()
    }
    private fun presentSuccErrMsg(s: String, isGood: Boolean?) {
        val toast = Toast.makeText(
            applicationContext,
            s,
            Toast.LENGTH_LONG
        )
        val toastView = toast.view
        toastView.setBackgroundResource(R.color.colorInvBackgroundDef)
        val v =
            toast.view.findViewById<View>(android.R.id.message) as TextView
        if (isGood == null){
            v.setTextColor(Color.parseColor("#FFA500"))
        } else {
            if (isGood!!) {
                v.setTextColor(Color.GREEN)
            } else {
                v.setTextColor(Color.RED)
            }
        }
        toast.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (intent?.data != null) {
            val segs = intent.data!!.pathSegments
            if (segs != null && segs.size == 1) {
                Model.getLiveData(segs.get(0)){
                    if (it != null) {
                        readyToPresentLiveVC(it!!)
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mAuthMessageReceiver,
            IntentFilter(Global.LocalStore.getAuthState())
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(mAuthFinished,
            IntentFilter(Global.LocalStore.getFinishedLiveState())
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(mAuthPostTip,
            IntentFilter(Global.LocalStore.getPostTipState())
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(mAuthErr,
            IntentFilter(Global.LocalStore.getErrorState())
        )
        mviewModel = ViewModelProviders.of(this).get(ViewModelMainAct::class.java)
        mviewModel.myViewCallBack = WeakReference(this)
        auth.removeAllViews()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.auth,authvc)
        ft.commit()
        showlive.removeAllViews()
        val ft2 = supportFragmentManager.beginTransaction()
        ft2.replace(R.id.showlive,livevc)
        ft2.commit()
        featurelefthead.setPaintFlags(featurelefthead.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        featuremiddlehead.setPaintFlags(featuremiddlehead.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        featurerighthead.setPaintFlags(featurerighthead.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
//        seg_one.setOnClickListener {
//            shouldEnableDarkMode(DarkModeConfig.FOLLOW_SYSTEM)
//            recreate()
//        }
        seg_two.setOnClickListener {
            shouldEnableDarkMode(DarkModeConfig.NO)
            recreate()
        }
        seg_three.setOnClickListener {
            shouldEnableDarkMode(DarkModeConfig.YES)
            recreate()
        }
        emailbtn.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("mailto:hello@streamself.live"))
            startActivity(browserIntent)
        }
        privbtn.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://streamself.live/privacy-policy"))
            startActivity(browserIntent)
        }
        initInstruction()
        val sharedPreferences = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        val retrievedInteger =
            sharedPreferences.getInt(Global.LocalStore.getDarkMode(),
                0)
        if (retrievedInteger == 1) {
            shouldEnableDarkMode(DarkModeConfig.YES)
        } else {
            shouldEnableDarkMode(DarkModeConfig.NO)
//        } else {
//            shouldEnableDarkMode(DarkModeConfig.FOLLOW_SYSTEM)
        }

    }
    private fun newHeightOfShowliveLayout(height: Int){
        val layoutParams = showliveparent.getLayoutParams()
        layoutParams.height = height
        showliveparent.setLayoutParams(layoutParams)
    }
    private fun setLiveContent(){
        val signInInfo = (getApplicationContext() as MainApp).getSignInUserInfo()
        if (signInInfo.first != null || signInInfo.second == null) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val layoutParams = showlive.getLayoutParams()
            livevc.setState(livecontent.StateOfLIVE.noneOfAbove)
            layoutParams.height = 0
            newHeightOfShowliveLayout(0)
            showlive.setLayoutParams(layoutParams)
        } else {
            if(mviewModel.shouldCallLoad) {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val layoutParams = showlive.getLayoutParams()
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                newHeightOfShowliveLayout(LinearLayout.LayoutParams.WRAP_CONTENT)
                showlive.setLayoutParams(layoutParams)
                goToRefreshLive()
                mviewModel.shouldCallLoad = false
            } else {
                nextStepOfLives()
            }
        }
    }
    private fun nextStepOfLives(){
        if (mviewModel.isCreatorLiveDataLoading || mviewModel.isAttendeeLiveDataLoading) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val layoutParams = showlive.getLayoutParams()
            livevc.setState(livecontent.StateOfLIVE.waitingspin)
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            newHeightOfShowliveLayout(LinearLayout.LayoutParams.WRAP_CONTENT)
            showlive.setLayoutParams(layoutParams)
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
//            val height = displayMetrics.heightPixels
            val layoutParams = showlive.getLayoutParams()
            if (mviewModel.isAttendeeLiveError || mviewModel.isCreatorLiveError){
                livevc.setState(livecontent.StateOfLIVE.errfound)
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                newHeightOfShowliveLayout(LinearLayout.LayoutParams.WRAP_CONTENT)
            } else if (mviewModel.attendeelivedata.size == 0 && mviewModel.creatorlivedata == null) {
                livevc.setState(livecontent.StateOfLIVE.noresfound)
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                newHeightOfShowliveLayout(LinearLayout.LayoutParams.WRAP_CONTENT)
            } else {
                livevc.setState(livecontent.StateOfLIVE.resfound)
                var hasMedia = 0
                var totalCount = 0
                for (al in mviewModel.attendeelivedata) {
                    if (al.mediaUrls.size > 0) {
                        hasMedia += 1
                    }
                    totalCount += 1
                }
                if (mviewModel.creatorlivedata != null) {
                    if (mviewModel.creatorlivedata!!.mediaUrls.size > 0) {
                        hasMedia += 1
                    }
                    totalCount += 1
                }
                val newHeightOfElem = FrameLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = newHeightOfElem
                newHeightOfShowliveLayout(newHeightOfElem)
            }
            showlive.setLayoutParams(layoutParams)
        }

    }
    override fun onRestart() {
        super.onRestart()
//        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAuthMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAuthErr);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAuthFinished);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAuthPostTip);
    }
    override fun onResume() {
        super.onResume()
        if (mviewModel.shouldCallLogin) {
            authvc.runAuth()
            mviewModel.shouldCallLogin = false
        } else {
            setLiveContent()
        }
        refreshAuth(false)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val myfeatureWidth = max(width - 400, 50)
        val layoutParams = featurelefthead.getLayoutParams()
        layoutParams.width = myfeatureWidth
        featurelefthead.setLayoutParams(layoutParams)

        val layoutParams2 = featuremiddlehead.getLayoutParams()
        layoutParams2.width = min((width - myfeatureWidth) / 2, 200)
        featuremiddlehead.setLayoutParams(layoutParams2)

        val layoutParams3 = featurerighthead.getLayoutParams()
        layoutParams3.width = min((width - myfeatureWidth) / 2, 200)
        featurerighthead.setLayoutParams(layoutParams3)

        val layoutParams4 = emailbtn.getLayoutParams()
        layoutParams4.width = min((width - 0) / 2, 500)
        emailbtn.setLayoutParams(layoutParams4)

        val layoutParams5 = privbtn.getLayoutParams()
        layoutParams5.width = min((width - 0) / 2, 500)
        privbtn.setLayoutParams(layoutParams5)

        val layoutParams6 = landingimgview.getLayoutParams()
        layoutParams6.width = min(layoutParams6.width, width - 20)
        landingimgview.setLayoutParams(layoutParams6)

        if (instructionAdapter != null) {
            instructionAdapter.widthContainer(width)
        }
        if (livevc.liveAdapter != null) {
            livevc.liveAdapter!!.widthContainer(width)
        }
        checkIfCanShowPostTip()
    }


    private fun refreshAuth(force: Boolean){
        if (force || authvc.getAuthState() != LoginElem.StateOfAuth.waitingspin) {
            authvc.updateAuthState((getApplicationContext() as MainApp).getSignInUserInfo())
        }
    }
    private fun initInstruction() {
        val rvContacts = instructionrecylce as RecyclerView
        instructionAdapter = InstructionAdapter()
        var data: MutableList<InstructionModel> = ArrayList()
        data.add(InstructionModel("Sign In / Sign Up", false, true))
        data.add(InstructionModel("Create your LIVE", false, true))
        data.add(InstructionModel("Register for LIVEs", false, true))
        data.add(InstructionModel("View Upcoming Lives",  true,  true))
        data.add(InstructionModel("During the LIVE, Turn Video and Audio On",  true,  true))
        data.add(InstructionModel("Chat and Tip during the LIVE",  true,  true))
        data.add(InstructionModel("Manage Access while LIVE",  true,  true))
        data.add(InstructionModel("Tip / Feedback after the LIVE",  true,  true))
        instructionAdapter.setMyFilling(data)
        rvContacts.adapter = instructionAdapter
        rvContacts.layoutManager = LinearLayoutManager(this)
    }
    private fun shouldEnableDarkMode(darkModeConfig: DarkModeConfig){
        var numberToSave = 0
        when(darkModeConfig){
            DarkModeConfig.YES -> {
//                seg_one.setBackgroundResource(R.color.colorBackgroundDef)
                seg_two.setBackgroundResource(R.color.colorBackgroundDef)
                seg_three.setBackgroundResource(R.color.mainbluecolor)
                numberToSave = 1
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            DarkModeConfig.NO -> {
//                seg_one.setBackgroundResource(R.color.colorBackgroundDef)
                seg_two.setBackgroundResource(R.color.mainbluecolor)
                seg_three.setBackgroundResource(R.color.colorBackgroundDef)
                numberToSave = 0
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
//            DarkModeConfig.FOLLOW_SYSTEM -> {
//                seg_one.setBackgroundResource(R.color.mainbluecolor)
//                seg_two.setBackgroundResource(R.color.colorBackgroundDef)
//                seg_three.setBackgroundResource(R.color.colorBackgroundDef)
//                numberToSave = 0
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            }
        }
        val sharedPreferences = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(Global.LocalStore.getDarkMode(), numberToSave)
            apply()
        }
    }

    override fun goToAuth() {
        val myIntent = Intent(this, actionauth::class.java)
        this.startActivity(myIntent)
    }

    override fun goToRefreshLive() {
        if (!mviewModel.isCreatorLiveDataLoading) {
            livevc.setState(livecontent.StateOfLIVE.waitingspin)
            mviewModel.isCreatorLiveDataLoading = true
            Model.getLiveInfo(true){ first, second ->
                mviewModel.isCreatorLiveDataLoading = false
                if (first != null) {
                    mviewModel.isCreatorLiveError = true
                    mviewModel.creatorlivedata = null
                } else {
                    mviewModel.isCreatorLiveError = false
                    mviewModel.creatorlivedata = if (second.size == 1) second.get(0) else null
                }
                mviewModel.callCBLive()
            }
        }
        if (!mviewModel.isAttendeeLiveDataLoading) {
            livevc.setState(livecontent.StateOfLIVE.waitingspin)
            mviewModel.isAttendeeLiveDataLoading = true
            Model.getLiveInfo(false){ first, second ->
                mviewModel.isAttendeeLiveDataLoading = false
                if (first != null) {
                    mviewModel.isAttendeeLiveError = true
                    mviewModel.attendeelivedata.clear()
                } else {
                    mviewModel.isAttendeeLiveError = false
                    mviewModel.attendeelivedata = second
                }
                mviewModel.callCBLive()
            }
        }
    }

    override fun getLivesCt(): Int {
        return mviewModel.attendeelivedata.size + (if(mviewModel.creatorlivedata != null) 1 else 0)
    }

    override fun getLiveAtCt(at: Int): LiveModel {
        if (mviewModel.creatorlivedata != null) {
            if (at == 0) {
                return mviewModel.creatorlivedata!!
            } else {
                return mviewModel.attendeelivedata.get(at - 1)
            }
        } else {
            return mviewModel.attendeelivedata.get(at)
        }
    }

    override fun getLiveByLiveId(liveid: String): LiveModel? {
        if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid) {
            return mviewModel.creatorlivedata!!
        } else {
            for (am in mviewModel.attendeelivedata) {
                if(am.liveIdval == liveid) {
                    return am
                }
            }
        }
        return null
    }

    override fun readyToPresentLiveVC(lm: LiveModel) {
        val myIntent = Intent(this, videolive::class.java)
        myIntent.putExtra("pdtitle", lm.title)
        myIntent.putExtra("pdusername", lm.usernameval)
        myIntent.putExtra("pdcreator", lm.iAmTheCreator)
        myIntent.putExtra("pdliveid", lm.liveIdval)
        this.startActivity(myIntent)
    }

    override fun changeImageIndex(liveid: String, goLeft: Boolean) {
        if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid) {
            var newInd = mviewModel.creatorlivedata!!.index + (if (goLeft) -1 else 1)
            if (newInd < 0) {
                newInd = mviewModel.creatorlivedata!!.mediaUrls.size - 1
            } else if (newInd > mviewModel.creatorlivedata!!.mediaUrls.size - 1){
                newInd = 0
            }
            mviewModel.creatorlivedata!!.index = newInd
        } else {
            for (i in 0 until mviewModel.attendeelivedata.size) {
                val am = mviewModel.attendeelivedata.get(i)
                if(am.liveIdval == liveid) {
                    var newInd = am.index + (if (goLeft) -1 else 1)
                    if (newInd < 0) {
                        newInd = am.mediaUrls.size - 1
                    } else if (newInd > am.mediaUrls.size - 1){
                        newInd = 0
                    }
                    mviewModel.attendeelivedata.get(i).index = newInd
                }
            }
        }
    }

    override fun isTheLiveMyOwn(liveid: String): Boolean {
        return mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid
    }

    override fun fetchImage(liveid: String, isUsernameImage: Boolean, mediaUrl: String) {
        if (isUsernameImage) {
            if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid && mviewModel.creatorlivedata!!.userimage != null) {
                mviewModel.creatorlivedata!!.userimage!!.state = MediaUrlLoadState.Waiting
            } else {
                var ind: Int? = null
                var i = 0
                for (am in mviewModel.attendeelivedata) {
                    if (am.liveIdval == liveid) {
                        ind = i
                    }
                    i += 1
                }
                if (ind != null && mviewModel.attendeelivedata.get(ind).userimage != null) {
                    mviewModel.attendeelivedata.get(ind).userimage!!.state = MediaUrlLoadState.Waiting
                } else {
                    return
                }
            }
        } else {
            if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid) {
                var j:Int? = null
                var ctr = 0
                for (mu in mviewModel.creatorlivedata!!.mediaUrls) {
                    if(mu.mediaurl == mediaUrl) {
                        j = ctr
                    }
                    ctr += 1
                }
                if(j != null) {
                    mviewModel.creatorlivedata!!.mediaUrls.get(j!!).state = MediaUrlLoadState.Waiting
                } else {
                    return
                }

            } else {
                var ind: Int? = null
                var i = 0
                for (am in mviewModel.attendeelivedata) {
                    if (am.liveIdval == liveid) {
                        ind = i
                    }
                    i += 1
                }
                if (ind != null) {
                    var j:Int? = null
                    var ctr = 0
                    for (mu in mviewModel.attendeelivedata.get(ind!!).mediaUrls) {
                        if(mu.mediaurl == mediaUrl) {
                            j = ctr
                        }
                        ctr += 1
                    }
                    if(j != null) {
                        mviewModel.attendeelivedata.get(ind!!).mediaUrls.get(j!!).state = MediaUrlLoadState.Waiting
                    } else {
                        return
                    }


                } else {
                    return
                }
            }
        }
        Model.downlaodImageAtUrl(mediaUrl) {
            val imgState = if(it != null) MediaUrlLoadState.Loaded else MediaUrlLoadState.Error
            if (isUsernameImage) {
                if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid && mviewModel.creatorlivedata!!.userimage != null) {
                    mviewModel.creatorlivedata!!.userimage!!.state = imgState
                    mviewModel.creatorlivedata!!.userimage!!.data = it
                } else {
                    var ind1: Int? = null
                    var i = 0
                    for (am in mviewModel.attendeelivedata) {
                        if (am.liveIdval == liveid) {
                            ind1 = i
                        }
                        i += 1
                    }
                    if (ind1 != null && mviewModel.attendeelivedata.get(ind1!!).userimage != null) {
                        mviewModel.attendeelivedata.get(ind1!!).userimage!!.state = imgState
                        mviewModel.attendeelivedata.get(ind1!!).userimage!!.data = it
                    }
                }
            } else {
                if (mviewModel.creatorlivedata != null && mviewModel.creatorlivedata!!.liveIdval == liveid) {
                    var j:Int? = null
                    var ctr = 0
                    for (mu in mviewModel.creatorlivedata!!.mediaUrls) {
                        if(mu.mediaurl == mediaUrl) {
                            j = ctr
                        }
                        ctr += 1
                    }
                    if(j != null) {
                        mviewModel.creatorlivedata!!.mediaUrls.get(j!!).state = imgState
                        mviewModel.creatorlivedata!!.mediaUrls.get(j!!).data = it
                    }
                } else {
                    var ind2: Int? = null
                    var i = 0
                    for (am in mviewModel.attendeelivedata) {
                        if (am.liveIdval == liveid) {
                            ind2 = i
                        }
                        i += 1
                    }
                    if (ind2 != null) {
                        var j:Int? = null
                        var ctr = 0
                        for (mu in mviewModel.attendeelivedata.get(ind2!!).mediaUrls) {
                            if(mu.mediaurl == mediaUrl) {
                                j = ctr
                            }
                            ctr += 1
                        }
                        if(j != null) {
                            mviewModel.attendeelivedata.get(ind2!!).mediaUrls.get(j!!).state = imgState
                            mviewModel.attendeelivedata.get(ind2!!).mediaUrls.get(j!!).data = it
                        }
                    }
                }
            }
            livevc.liveAdapter?.notifyDataSetChanged()
        }
    }

    override fun cbLive() {
        nextStepOfLives()
    }
}
// change endpoint