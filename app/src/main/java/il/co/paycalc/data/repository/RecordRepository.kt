package il.co.paycalc.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import il.co.paycalc.data.localDb.records.RecordDao
import il.co.paycalc.data.localDb.records.RecordDatabase
import il.co.paycalc.data.remoteDb.RecordRemoteDataSource
import il.co.paycalc.utils.Constants.RESOURCE_ID
import il.co.paycalc.data.api.HolidayApi
import il.co.paycalc.data.model.holiday.Holiday
import il.co.paycalc.utils.performFetchingAndSaving
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import il.co.paycalc.data.model.holiday.Record

class RecordRepository(
    private val apiInterface: HolidayApi,
    private val recordDatabase: RecordDatabase,
    private val remoteDatasource: RecordRemoteDataSource,
    private val recordDao: RecordDao,
) {

    private val recordsLiveData = MutableLiveData<Holiday>()

    val records: LiveData<Holiday>
        get() = recordsLiveData


    suspend fun getRecords() {
        val result = apiInterface.getHoliday(RESOURCE_ID)

        if (result.body() != null) {
            recordDatabase.recordDao().clearRecords()
            recordDatabase.recordDao().insertRecord(result.body()!!.result.records!!)
            recordsLiveData.postValue(result.body())
        }
    }


    fun getHolidays() = performFetchingAndSaving(
        { recordDao.getRecords() },
        { remoteDatasource.getRecords(RESOURCE_ID) },
        { recordDao.clearRecords() },
        { recordDao.insertRecord(it.result.records!!) }
    )

    fun clearRecords() {
        recordDatabase.recordDao().clearRecords()
    }

}