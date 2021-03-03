package kreios828.com.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_rating.*
import kreios828.com.R

class RatingActivity : BaseActivity(0) {
    private val TAG = "RatingActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")


        cezve_rating_btn.setOnClickListener {
            val intent = Intent(this, CezveRatingActivity::class.java)
            startActivity(intent)
        }

        mocha_rating_btn.setOnClickListener {
            val intent = Intent(this, MochaRatingActivity::class.java)
            startActivity(intent)
        }

        espresso_rating_btn.setOnClickListener {
            val intent = Intent(this, EspressoRatingActivity::class.java)
            startActivity(intent)
        }

        automatic_rating_btn.setOnClickListener {
            val intent = Intent(this, AutomaticRatingActivity::class.java)
            startActivity(intent)
        }


        mAuth = FirebaseAuth.getInstance()
//        mAuth.signOut()
        /*auth.signInWithEmailAndPassword("ser@serlakh.ru", "password")
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d(TAG, "signIn: success")
                } else {
                    Log.e(TAG, "signIn: failure", it.exception)
                }
            }*/

        sign_out_text.setOnClickListener {
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }


        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
