package kreios828.com.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_mocha_rating.*
import kotlinx.android.synthetic.main.rating_item.view.*
import kreios828.com.R
import kreios828.com.models.RatingPost
import kreios828.com.utills.FireBaseHelper
import kreios828.com.utills.ValueEventListenerAdapter

class MochaRatingActivity : AppCompatActivity(), RatingAdapter.Listener {
    private val TAG = "MochaRatingActivity"
    private lateinit var mFirebase: FireBaseHelper
    private lateinit var mAdapter: RatingAdapter
    private var mLikesListeners: Map<String, ValueEventListener> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mocha_rating)

        Log.d(TAG, "onCreate")



        close_image.setOnClickListener {
            finish()
        }

        mFirebase = FireBaseHelper(this)
        mFirebase.database.child("rating-posts")
            .child("mocha_rating")           /*.child(mFirebase.currentUid()!!)*/
            .addValueEventListener(ValueEventListenerAdapter {
                val posts = it.children.map { it.asRatingPost()!! }
                Log.d(TAG, "MochaRatingPosts: ${posts.joinToString("\n", "\n")} ")
                mAdapter = RatingAdapter(this, posts)
                rating_recycler.adapter = mAdapter
                rating_recycler.layoutManager = LinearLayoutManager(this)
            })
    }

    override fun toggleLike(postId: String) {
        Log.d(TAG, "toggleLike: $postId")
        val reference =
            mFirebase.database.child("likes").child(postId).child(mFirebase.currentUid())
        reference
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                reference.setValueTrueOrRemove(!it.exists())
            })
    }

    override fun loadLikes(postId: String, position: Int) {
        fun createListener() =
            mFirebase.database.child("likes").child(postId).addValueEventListener(
                ValueEventListenerAdapter {
                    val userLikes = it.children.map { it.key }.toSet()
                    val postLikes = RatingPostLikes(
                        userLikes.size,
                        userLikes.contains(mFirebase.currentUid())
                    ) // если id пользователя находится в ID пролайкавших, то он пролайкал
                    mAdapter.updatePostLikes(position, postLikes)
                })

        val createNewListener = mLikesListeners[postId] == null
        Log.d(TAG, "loadLikes: $position $createNewListener")
        if (createNewListener) {
            mLikesListeners += (postId to createListener())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLikesListeners.values.forEach { mFirebase.database.removeEventListener(it) }
    }
}

data class RatingPostLikes(val likesCount: Int, val likes: Boolean)

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