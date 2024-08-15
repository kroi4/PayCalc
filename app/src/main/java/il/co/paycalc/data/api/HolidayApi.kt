package il.co.paycalc.data.api

import il.co.paycalc.data.model.holiday.Holiday
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HolidayApi {

    @GET("action/datastore_search")
    suspend fun getHoliday(
        @Query("resource_id") resourceId:String,
    ): Response<Holiday>

}