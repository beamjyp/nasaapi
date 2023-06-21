package com.example.nasa

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

data class ApodData(
    val date: String,
    val explanation: String,
    val url: String,
    val hdurl: String
)

interface NasaApiService {
    @GET("planetary/apod")
    fun getApodData(
        @Query("api_key") apiKey: String,
        @Query("date") date: String
    ): Call<ApodData>
}

class MainActivity : AppCompatActivity() {
    private lateinit var explanationTextView: TextView

    private val apiKey = "qfV9hXMHLSFDvZJotr425xUs3rBzfSEfzhz2tQFG"
    private val baseUrl = "https://api.nasa.gov/"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        explanationTextView = findViewById(R.id.explanationTextView)

        val datePickerButton: Button = findViewById(R.id.datePickerButton)
        datePickerButton.setOnClickListener { showDatePickerDialog() }

        // Get APOD data for current date
        val currentDate = dateFormat.format(Date())
        getApodData(currentDate)
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = dateFormat.format(selectedDate.time)
            getApodData(formattedDate)
        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun getApodData(date: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(NasaApiService::class.java)
        val call = apiService.getApodData(apiKey, date)

        call.enqueue(object : Callback<ApodData> {
            override fun onResponse(call: Call<ApodData>, response: Response<ApodData>) {
                if (response.isSuccessful) {
                    val apodData = response.body()
                    if (apodData != null) {
                        explanationTextView.text = apodData.explanation
                        loadImageFromUrl(apodData.url)
                    }
                } else {
                    explanationTextView.text = "Request failed with code ${response.code()}"
                }
            }

            override fun onFailure(call: Call<ApodData>, t: Throwable) {
                explanationTextView.text = "Error occurred: ${t.message}"
            }
        })
    }

    private fun loadImageFromUrl(imageUrl: String) {
        val imageView: ImageView = findViewById(R.id.imageView)

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }
}



