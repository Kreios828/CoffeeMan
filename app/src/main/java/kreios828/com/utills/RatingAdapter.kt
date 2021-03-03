package kreios828.com.utills

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rating_item.view.*
import kreios828.com.R
import kreios828.com.activities.RatingPostLikes
import kreios828.com.activities.showToast
import kreios828.com.models.RatingPost

class RatingAdapter(private val listener: Listener, private val posts: List<RatingPost>) :
    RecyclerView.Adapter<RatingAdapter.ViewHolder>() {

    interface Listener {
        fun toggleLike(postId: String) //функция переключает лайки(вкл/выкл)
        fun loadLikes(id: String, position: Int)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private var postLikes: Map<Int, RatingPostLikes> = emptyMap()
    private val defaultPostLikes = RatingPostLikes(0, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rating_item, parent, false)
        return ViewHolder(view)
    }

    fun updatePostLikes(position: Int, likes: RatingPostLikes) {
        postLikes += (position to likes)
        notifyItemChanged(position)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        val likes = postLikes[position] ?: defaultPostLikes
        with(holder.view) {
            user_photo_image.loadImage(post.photo)
            username_text.text = post.username
            post_image.loadImage(post.image)
            if (likes.likesCount == 0) {
                likes_text.visibility = View.GONE
            } else {
                likes_text.visibility = View.VISIBLE
                likes_text.text = "${likes.likesCount} likes"
            }
            caption_text.setCaptionText(post.username, post.caption) // делает текст кликабельным
            like_image.setOnClickListener { listener.toggleLike(post.id) }
            like_image.setImageResource(
                if (likes.likes) R.drawable.ic_favorit
                else R.drawable.ic_likes_border
            )
            listener.loadLikes(post.id, position)
        }
    }

    private fun TextView.setCaptionText(username: String, caption: String) {
        val usernameSpannable = SpannableString(username)
        usernameSpannable.setSpan(
            StyleSpan(Typeface.BOLD), 0, usernameSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        usernameSpannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.context.showToast("Username is clicked")
            }

            override fun updateDrawState(ds: TextPaint) {}
        }, 0, usernameSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text = SpannableStringBuilder().append(usernameSpannable).append(" ")
            .append(caption)
        movementMethod = LinkMovementMethod.getInstance() // делает текст кликабельным
    }

    override fun getItemCount() = posts.size

    private fun ImageView.loadImage(image: String?) {
        Glide.with(this).load(image).centerCrop().into(this)
    }


}