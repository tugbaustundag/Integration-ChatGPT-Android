package com.smality.chatgptandroid

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    lateinit var responseTV: TextView
    lateinit var questionTV: TextView
    lateinit var queryEdt: TextInputEditText

    var url = "https://api.openai.com/v1/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        responseTV = findViewById(R.id.idTVResponse)
        questionTV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)

        //Edit text'e yazılan soruyu alalım.
        queryEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                responseTV.text = "Please wait.."
                if (queryEdt.text.toString().length > 0) {
                    //Cevabı alabilmek için web servise soruyu gönderme
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query..", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }
    //OpenAI web servisinden cevap verisini çekme
    private fun getResponse(query: String) {
        //Soruyu ekranda gösterelim
        questionTV.text = query
        queryEdt.setText("")

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        //Json objesini oluşturup, OpenAI talep ettiği parametreleri belirtiyoruz.
        val jsonObject: JSONObject? = JSONObject()
        jsonObject?.put("model", "gpt-3.5-turbo-instruct")
        jsonObject?.put("prompt", query)
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_tokens", 500)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

        //Volley kütüphanesinde POST metodu ile request tanımlıyoruz
        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    //ChatGPT'den gelen sorunun cevabını text view atama
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                    responseTV.text = responseMsg
                },
                //Bir hata varsa onu log yapma
                Response.ErrorListener { error ->
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    //Headers ve API key ekleme
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer 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"
                    return params;
                }
            }

        // Volley'in retry policy ekliyoruz.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }
            override fun getCurrentRetryCount(): Int {
                return 50000
            }
            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })

        queue.add(postRequest)
    }
}