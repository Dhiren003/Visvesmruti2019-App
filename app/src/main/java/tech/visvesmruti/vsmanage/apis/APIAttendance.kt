package tech.visvesmruti.vsmanage.apis

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.widget.Toast

import tech.visvesmruti.vsmanage.Consts
import com.appizona.yehiahd.fastsave.FastSave

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import tech.visvesmruti.vsmanage.activities.AttendanceDoneActivity
import tech.visvesmruti.vsmanage.activities.PaymentCheckActivity

class APIAttendance internal constructor(private val activity: Activity) : AsyncTask<String, Void, String>() {
    private val client: OkHttpClient
    private var dialog: ProgressDialog? = null
    private lateinit var ercode: String

    init {
        this.client = OkHttpClient()
    }

    override fun onPreExecute() {
        dialog = ProgressDialog(activity)
        dialog!!.setMessage("Attendence Checking...")
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    override fun doInBackground(vararg strings: String): String? {
        ercode = strings[0]
        val formBody = FormBody.Builder()
                .add("apikey", FastSave.getInstance().getString(Consts.TOKEN, ""))
                .add("ercode", ercode)
                .build()

        val request = Request.Builder()
                .url("https://visvesmruti.tech/api/event/attend/")
                .post(formBody)
                .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return response.body!!.string()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(s: String?) {
        dialog!!.cancel()
        if (s == null) {
            Toast.makeText(activity, "Error!, Internet", Toast.LENGTH_LONG).show()
        } else {
            try {
                val result = JSONObject(s)
                val code = result.getInt("Code")
                if (code == 200) {
                    val i: Intent
                    if(result.getInt(Consts.ISPAID) == 1){
                        i = Intent(activity, AttendanceDoneActivity::class.java)
                    } else {
                        i = Intent(activity, PaymentCheckActivity::class.java)
                        i.putExtra(Consts.ERCODE, ercode)
                    }
                    i.putExtra(Consts.NAME, result.getString(Consts.NAME))
                    i.putExtra(Consts.EVENT, result.getString(Consts.EVENT))
                    i.putExtra(Consts.EMAIL, result.getString(Consts.EMAIL))
                    activity.startActivity(i)
                    activity.finish()
                } else if (code == 404) {
                    Toast.makeText(activity, result.getString("Message"), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity, "Error!, Wrong Status", Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(activity, "Error!, Result Problem", Toast.LENGTH_LONG).show()
            }

        }
    }
}
