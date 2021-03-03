package kreios828.com.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_add.*
import kreios828.com.R
import kreios828.com.models.RatingPost
import kreios828.com.models.User
import kreios828.com.utills.CameraHelper
import kreios828.com.utills.FireBaseHelper
import kreios828.com.utills.ValueEventListenerAdapter

class AddActivity : BaseActivity(2), AdapterView.OnItemSelectedListener {
    private val TAG = "AddActivity"
    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FireBaseHelper
    private lateinit var mUser: User

    private var spinner: Spinner? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private var result: TextView? = null
    private var itemList = arrayOf(
        "рейтинг для турки",
        "рейтинг для мокки",
        "рейтинг для рожка",
        "рейтинг для автомата"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        Log.d(TAG, "onCreate")

        mFirebase = FireBaseHelper(this)
        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        back_image.setOnClickListener { finish() }
        share_text.setOnClickListener { share() }

        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
        })


        spinner = findViewById(R.id.spinners)
        result = findViewById(R.id.resultText)
        arrayAdapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, itemList)
        spinner?.adapter = arrayAdapter
        spinner?.onItemSelectedListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        if (requestCode == mCamera.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Glide.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCamera.imageUri

        val ratingSelector =
            when (result?.text) {
                "рейтинг для турки" -> "cezve_rating"
                "рейтинг для мокки" -> "mocha_rating"
                "рейтинг для рожка" -> "espresso_rating"
                "рейтинг для автомата" -> "automatic_rating"
                else -> {
                    Log.e(TAG, "Unknown item_clicked")
                    null
                }
            }

        if (imageUri != null) {
            val uid = mFirebase.currentUid()!!
            mFirebase.storage.child("users").child(uid).child("images")
                .child(imageUri.lastPathSegment.toString()).putFile(imageUri)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imageDownloadUrl = it.result.downloadUrl!!.toString()
                        mFirebase.database.child("images").child(uid).push()
                            .setValue(imageDownloadUrl)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    mFirebase.database.child("rating-posts")
                                        .child(ratingSelector)         /*child(uid)*/
                                        .push()
                                        .setValue(mkFeesPost(uid, imageDownloadUrl))
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        ProfileActivity::class.java
                                                    )
                                                )
                                                finish()
                                            }
                                        }
                                } else {
                                    showToast(it.exception!!.message!!)
                                }
                            }
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
        }
    }

    private fun mkFeesPost(
        uid: String,
        imageDownloadUrl: String
    ): RatingPost {
        return RatingPost(
            uid = uid, // наименование параметров
            username = mUser.username,
            image = imageDownloadUrl,
            caption = caption_input.text.toString(),
            photo = mUser.photo
        )
    }


    //для Spinnera
    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(applicationContext, "Nothing selected", Toast.LENGTH_LONG).show()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var items: String = parent?.getItemAtPosition(position) as String
        result?.text = items
        //Toast.makeText(applicationContext, "$items", Toast.LENGTH_LONG).show()
    }

}

