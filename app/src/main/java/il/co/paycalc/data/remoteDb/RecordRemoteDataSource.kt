package il.co.paycalc.data.remoteDb

import il.co.paycalc.data.api.FlightsApi

class RecordRemoteDataSource(
    private val apiService: FlightsApi
) : BaseDataSource() {

    suspend fun getRecords(resourceId: String) = getResult { apiService.getFlights(resourceId) }
}