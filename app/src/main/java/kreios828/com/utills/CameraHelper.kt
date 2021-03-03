package kreios828.com.utills

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val activity: Activity) {
    var imageUri: Uri? = null
    val REQUEST_CODE = 1 // Константа для проверки в onActivity Result
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun takeCameraPicture() {
        val intent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE) // по клику на change photo выполняем intent ACTION_IMAGE_CAPTURE
        if (intent.resolveActivity(activity.packageManager) != null) { //проверка если у пользователя нет камеры - мы не можем сделать камеру - берем активити - resolveActivity
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                activity,
                "kreios828.com.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun createImageFile(): File {
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}