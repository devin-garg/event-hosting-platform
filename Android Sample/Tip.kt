package live.streamself.streamselflive

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tip.view.*
import live.streamself.streamselflive.R
import okhttp3.internal.wait
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class tip : Fragment(), ViewModelTipAct.TipCallback, TipAdapter.TipAdapterCallback {
    private lateinit var tipAdapter: TipAdapter
    private lateinit var mviewModel: ViewModelTipAct
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mviewModel = ViewModelProviders.of(this).get(ViewModelTipAct::class.java)
        mviewModel.myViewCallBack = WeakReference(this)
    }
    private var waitingSpinnerView: ProgressBar? = null
    private var recview: RecyclerView? = null
    private var waitingContainer: LinearLayout? = null
    private var errorContainer: LinearLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tip, container, false)
        val rvContacts = view.tiprecycle as RecyclerView
        tipAdapter = TipAdapter()
        tipAdapter.setCallback(this)
        rvContacts.adapter = tipAdapter
        rvContacts.layoutManager = LinearLayoutManager(activity)
        view.submitretry.setOnClickListener {
            mviewModel.runTipState(activity)
        }
        recview = view.tiprecycle
        waitingContainer = view.waitingcontainer
        errorContainer = view.errorcontainer
        waitingSpinnerView = view.waitingspin
        if(mviewModel.getMyTipState() == ViewModelTipAct.TipState.notloaded) {
            mviewModel.runTipState(activity)
        }
        stateChanged(mviewModel.getMyTipState())
        return view
    }
    override fun onPause() {
        super.onPause()
        if (mviewModel.getMyTipState() == ViewModelTipAct.TipState.waiting) {
            waitingSpinnerView?.setVisibility(View.GONE)
        }
    }

    override fun onResume() {
        super.onResume()
        stateChanged(mviewModel.getMyTipState())
    }
    fun getPaymentError(activity: Activity, completion: (Payment?, Model.ModelErr?)->Unit){
        mviewModel.launchBillingFlow(activity, completion)
    }
    fun resetTipState() {
        mviewModel.selectedIndex = null
        tipAdapter.notifyDataSetChanged()
    }

    override fun stateChanged(state: ViewModelTipAct.TipState) {
        waitingContainer?.setVisibility(View.GONE)
        waitingSpinnerView?.setVisibility(View.GONE)
        errorContainer?.setVisibility(View.GONE)
        recview?.setVisibility(View.GONE)
        when (state){
            ViewModelTipAct.TipState.waiting->{
                waitingContainer?.setVisibility(View.VISIBLE)
                waitingSpinnerView?.setVisibility(View.VISIBLE)
            }
            ViewModelTipAct.TipState.error->{
                errorContainer?.setVisibility(View.VISIBLE)
            }
            ViewModelTipAct.TipState.resfound->{
                recview?.setVisibility(View.VISIBLE)
            }
        }
        tipAdapter.notifyDataSetChanged()
    }

    override fun getLivesCt(): Int {
        return  mviewModel.prodlists.size
    }

    override fun getLive(position: Int): Pair<SkuDetails, Boolean> {
        return  Pair(mviewModel.prodlists.get(position), if (mviewModel.selectedIndex != null) mviewModel.selectedIndex!! == position else false)
    }

    override fun skuClicked(sku: String) {
        var ind: Int? = null
        var i = 0
        for (pr in mviewModel.prodlists) {
            if (pr.sku == sku) {
                ind = i
            }
            i += 1
        }
        if (ind == mviewModel.selectedIndex) {
            mviewModel.selectedIndex = null
        } else {
            mviewModel.selectedIndex = ind
        }
        tipAdapter.notifyDataSetChanged()
    }
}