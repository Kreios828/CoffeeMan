package kreios828.com.activities

import android.os.Bundle
import android.util.Log
import kreios828.com.R

class FavouriteActivity : BaseActivity(1) {
    private val TAG = "FavouriteActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")
    }
}