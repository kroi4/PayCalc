package il.co.skystar.api

import il.co.paycalc.data.api.HolidayApi
import il.co.paycalc.utils.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: HolidayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HolidayApi::class.java)
    }
}