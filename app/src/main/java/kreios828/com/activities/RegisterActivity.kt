package kreios828.com.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import kreios828.com.R
import kreios828.com.models.User

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NamePassFragment.Listener {
    private val TAG = "RegisterActivity"

    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference // ссылка на объекты в БД

        if (savedInstanceState == null) {//проверка что активити создалась первый раз, чтобы не добавилось 2 фрагмента на экране
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment())
                .commit()
        }
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email) { signInMethods ->
                if (signInMethods.isEmpty()) { // если signInMethods == null, либо isEmpty = true(нет signInMethods)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, NamePassFragment())
                        .addToBackStack(null) //Предыдущий EmailFragment сохранится в BackStack, и если нажать назад, попадешь в него
                        .commit()
                } else {
                    showToast("This email already exists")
                }
            }
        } else {
            showToast("Please enter email")
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()) { // проверяем фулл нейм и пароль
            val email = mEmail
            if (email != null) { //проверяем пароль
                mAuth.createUserWithEmailAndPassword(
                    email,
                    password
                ) {//создаем пользователя с email и uid
                    mDatabase.createUser(it.user.uid, mkUser(fullName, email)) {
                        startHomeActivity()
                    }
                }
            } else {
                Log.e(TAG, "onRegister: email is null")
                showToast("Please enter email and password")
                supportFragmentManager.popBackStack()//возвращение пользователя на EmailFragment из BackStack в случае когда email == null
            }
        } else {
            showToast("Please enter full name and password")
        }
    }

    private fun unknownRegisterError(it: Task<*>) { // * означает, что принимаем любое значение
        Log.e(TAG, "failed to create user profile", it.exception)
        showToast("Something wrong happened. Please try again later")
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, RatingActivity::class.java))
        finish()

    }

    private fun mkUser(
        fullName: String,
        email: String
    ): User { //принмает fullname и email и возвращает User
        val username = mkUsername(fullName)
        return User(name = fullName, username = username, email = email)
    }

    private fun mkUsername(fullName: String) =
        fullName.toLowerCase().replace(" ", ".")

    private fun FirebaseAuth.fetchSignInMethodsForEmail(
        email: String,
        onSuccess: (List<String>) -> Unit
    ) {
        fetchSignInMethodsForEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(
                    it.result.signInMethods ?: emptyList<String>()
                ) //если пустые, то создадим Empty list
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit) {
        val reference = child("users").child(uid)
        reference.setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess
            } else {
                unknownRegisterError(it)
            }
        }
    }

    private fun FirebaseAuth.createUserWithEmailAndPassword(
        email: String, password: String,
        onSuccess: (AuthResult) -> Unit
    ) {
        createUserWithEmailAndPassword(
            email,
            password
        ) // создаём пользователя с email password и uid
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess(it.result)
                } else {
                    unknownRegisterError(it)
                }
            }
    }
}


// 1 - Email, next button
class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener { //Интерфейс, принимающий значение email'а. Будет имплементироваться в активити
        fun onNext(email: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) { //Метод для инициализации UI(можно получать ссылки на элементы интерфейса)
        coordinateBtnAndInputs(next_btn, email_input)

        next_btn.setOnClickListener {
            val email = email_input.text.toString()
            mListener.onNext(email) //Когда нажмется кнопка Next, будет получен email и передан в Listener
        }
    }

    override fun onAttach(context: Context) { // Метод для получения ссылки на активити(по сути - это активити)
        super.onAttach(context)
        mListener = context as Listener //каст до листенера
    }
}

// 2 - Full name, password, register button
class NamePassFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(fullName: String, password: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(register_btn, full_name_input, password_input)
        register_btn.setOnClickListener {
            val fullName = full_name_input.text.toString()
            val password = password_input.text.toString()
            mListener.onRegister(fullName, password)
        }
    }

    override fun onAttach(context: Context) { // Метод для получения ссылки на активити(по сути - это активити)
        super.onAttach(context)
        mListener = context as Listener //каст до листенера
    }
}