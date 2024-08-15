package il.co.paycalc.data.remoteDb

import il.co.paycalc.data.api.HolidayApi

class RecordRemoteDataSource(
    private val apiService: HolidayApi
) : BaseDataSource() {

    suspend fun getRecords(resourceId: String) = getResult { apiService.getHoliday(resourceId) }
}