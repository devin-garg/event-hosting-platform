package live.streamself.streamselflive

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.chat_rel_layout.view.*
import kotlinx.android.synthetic.main.instruction_rel_layout.view.*
import kotlinx.android.synthetic.main.video_rel_layout.view.*
import live.streamself.streamselflive.R
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max


class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface ChatAdapterCallback {
        fun getChatLivesCt(): Int
        fun getChatLive(position: Int): Chat
        fun chatSocketIdClick(socketId: String)
    }
    private var mAdapterCallback: WeakReference<ChatAdapterCallback>? = null
    public fun setCallback(callback: ChatAdapterCallback){
        mAdapterCallback = WeakReference(callback)
    }
    private var contWidth: Int? = null
    fun widthContainer(width: Int){
        contWidth = width
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewarg = LayoutInflater.from(parent.context).inflate(R.layout.chat_rel_layout, parent, false)
        val view = ChatViewHolder(
            viewarg,
            mAdapterCallback?.get()
        )
        return view
    }

    override fun getItemCount(): Int {
        val elem = mAdapterCallback?.get()
        return if(elem != null) elem.getChatLivesCt() else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatViewHolder -> {
                val elem = mAdapterCallback?.get()
                if (elem != null) {
                    holder.bind(elem.getChatLive(position), contWidth)
                }
            }
        }
    }

    class ChatViewHolder constructor(
        itemView: View,
        callback: ChatAdapterCallback?
    ): RecyclerView.ViewHolder(itemView) {
        private fun getTheTimeGlobal(myLong: Long, pastDate: Date) : String {
            val startDate = Date(myLong)
            val endDate = pastDate
            var different: Long = endDate.getTime() - startDate.getTime()
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val elapsedDays = different / daysInMilli
            different = different % daysInMilli
            val elapsedHours = different / hoursInMilli
            different = different % hoursInMilli
            val elapsedMinutes = different / minutesInMilli
            different = different % minutesInMilli
            val elapsedSeconds = different / secondsInMilli
            val start = "Sent "
            val ending = " ago"
            if (elapsedDays > 0) {
                if (elapsedHours > 0) {
                    return start + elapsedDays.toString() + " day"+ (if(elapsedDays.toInt() != 1) "s " else " ") + elapsedHours.toString() + " hr"+ (if(elapsedHours.toInt() != 1) "s" else "") + ending
                } else {
                    return start + elapsedDays.toString() + " day"+ (if(elapsedDays.toInt() != 1) "s" else "") + ending
                }
            } else if (elapsedHours > 0) {
                if (elapsedMinutes > 0) {
                    return start + elapsedHours.toString() + " hr"+ (if(elapsedHours.toInt() != 1) "s " else " ") + elapsedMinutes.toString() + " min" + ending
                } else {
                    return start + elapsedHours.toString() + " hr" + (if(elapsedHours.toInt() != 1) "s" else "") + ending
                }
            } else if (elapsedMinutes > 0) {
                if (elapsedSeconds > 0) {
                    return start + elapsedMinutes.toString() + " min " + elapsedSeconds.toString() + " sec" + ending
                } else {
                    return start + elapsedMinutes.toString() + " min" + ending
                }
            } else {
                return if (elapsedSeconds > 0) (start + elapsedSeconds.toString() + " sec" + ending) else "Now"
            }
        }
        private var mAdapterCallback: WeakReference<ChatAdapterCallback>? = if(callback != null) WeakReference(callback!!) else null
        init {
            itemView.setOnClickListener {
                val id = it.getTag() as? String
                if (id != null && !id!!.isEmpty()) {
                    mAdapterCallback?.get()?.chatSocketIdClick(id!!)
                }
            }
        }
        fun bind(im: Chat, width: Int?) {
            val width = width?.let {
                // If b is not null.

            } ?: run {
                // If b is null.
            }
            itemView.setTag(if(im.socketId == null) "" else im.socketId!!)
            val isMessage = im.price == null
            itemView.author.text = im.name
            if (im.spec) {
                itemView.author.setTextColor(Color.YELLOW)
            } else {
                itemView.author.setTextColor(Color.WHITE)
            }
//            itemView.date.setTextColor(Color.WHITE)
            itemView.date.text = getTheTimeGlobal(im.date, Date())
            if (im.area != null && im.area!!.length > 0) {
//                cell.messageChat.textColor = isMessage ? .white : .white
                itemView.msg.text = im.area!!
                itemView.msg.visibility = View.VISIBLE
            } else {
                itemView.msg.visibility = View.GONE
            }
            if (!isMessage) {
                itemView.tipamt.text = "Tipped $" + ((im.price!! * 100).toInt() / 100.0).toString()
                itemView.tipamt.visibility = View.VISIBLE
            } else {
                itemView.tipamt.visibility = View.GONE
            }
            if (!isMessage) {
                itemView.setBackgroundResource(R.color.maingreencolor)
            } else {
                itemView.setBackgroundColor(Color.parseColor("#00B2FF"))
            }
        }

    }
}