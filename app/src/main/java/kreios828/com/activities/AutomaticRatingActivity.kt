package kreios828.com.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_cezve_rating.*
import kreios828.com.R

class AutomaticRatingActivity : AppCompatActivity() {
    private val TAG = "AutomaticRatingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automatic_rating)
        Log.d(TAG, "onCreate")

        close_image.setOnClickListener {
            finish()
        }
    }
}