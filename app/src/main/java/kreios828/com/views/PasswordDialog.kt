package kreios828.com.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_password.view.*
import kreios828.com.R

class PasswordDialog : DialogFragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onPasswordConfirm(password: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_password, null) //получили view
        return AlertDialog.Builder(context!!) //создаем allrtDialog
            .setView(view) // задаем ему view
            .setPositiveButton(android.R.string.ok, { _, _ ->
                mListener.onPasswordConfirm(view.password_input.text.toString())
            })
            .setNegativeButton(android.R.string.cancel, { _, _ ->
                //do nothing
            })
            .setTitle(R.string.please_enter_password)
            .create()
    }
}