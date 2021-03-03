package kreios828.com.activities

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_cezve_rating.*
import kreios828.com.R
import kreios828.com.utills.FireBaseHelper
import kreios828.com.utills.RatingAdapter
import kreios828.com.utills.ValueEventListenerAdapter

class CezveRatingActivity : AppCompatActivity(), RatingAdapter.Listener {
    private val TAG = "CezveRatingActivity"
    private lateinit var mFirebase: FireBaseHelper
    private lateinit var mAdapter: RatingAdapter
    private var mLikesListeners: Map<String, ValueEventListener> = emptyMap()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cezve_rating)
        Log.d(TAG, "onCreate")

        close_image.setOnClickListener {
            finish()
        }

        mFirebase = FireBaseHelper(this)

        /*images_cezve_recycler.layoutManager = GridLayoutManager(this, 1)                          кусок кода для вывода своих постов
        mFirebase.database.child("images").child(mFirebase.currentUid()!!)
            .addValueEventListener(ValueEventListenerAdapter{
                val images = it.children.map { it.getValue(String::class.java)!! }
                images_cezve_recycler.adapter = ImagesAdapter(images)
        })*/


        mFirebase = FireBaseHelper(this)
        mFirebase.database.child("rating-posts").child("cezve_rating")
            .addValueEventListener(ValueEventListenerAdapter {
                val posts = it.children.map { it.asRatingPost()!! }
                Log.d(TAG, "CezveRatingPosts: ${posts.joinToString("\n", "\n")} ")
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
}

class ImagesAdapter(private val images: List<String>) : //ImagesAdapter позволяет засовывать картинки в recycler
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    class ViewHolder(val image: ImageView) :
        RecyclerView.ViewHolder(image) // ViewHolder паттерн, кэширует ImageView в памяти чтобы постоянно не искать его в layout и искать на него ссылку

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder { //метод просто создает ViewHolder
        val image = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) { //метод в котором нужно засунуть данные в holder
        holder.image.loadImage(images[position])
    }

    private fun ImageView.loadImage(image: String) {
        Glide.with(this).load(image).centerCrop().into(this)
    }

    override fun getItemCount(): Int = images.size
}

class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs)


/*class RatingAdapter(private val listener: RatingAdapter.Listener, private val posts: List<RatingPost>)
    : RecyclerView.Adapter<RatingAdapter.ViewHolder>() {

    interface Listener {
        fun toggleLike(postId: String)
        fun loadLikes(id: String, postition: Int)
    }

    class ViewHolder

}*/