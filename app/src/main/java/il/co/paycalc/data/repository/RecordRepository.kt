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

class RecordRepository(
    private val apiInterface: HolidayApi,
    private val recordDatabase: RecordDatabase,
    private val remoteDatasource: RecordRemoteDataSource,
    private val localDatasource: RecordDao, ) {

    private val recordsLiveData = MutableLiveData<Holiday>()

    val records: LiveData<Holiday>
        get() = recordsLiveData

//    suspend fun isHoliday(date: Date, recordDao: RecordDao): Boolean {
//        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        val dateAsString = date.format(dateFormat)
//        val record = recordDao.getHolidayByDate(dateAsString)
//        return record != null && record.Name.isNotEmpty()
//    }


    suspend fun getRecords() {
        val result = apiInterface.getHoliday(RESOURCE_ID)

        if (result.body() != null) {
            recordDatabase.recordDao().clearRecords()
            recordDatabase.recordDao().insertRecord(result.body()!!.result.records!!)
            recordsLiveData.postValue(result.body())
        }
    }



    fun getDepartures() = performFetchingAndSaving(
        {localDatasource.getRecords()},
        {remoteDatasource.getRecords(RESOURCE_ID)},
        {localDatasource.clearRecords()},
        {localDatasource.insertRecord(it.result.records!!)}
    )

    fun clearRecords() {
        recordDatabase.recordDao().clearRecords()
    }

}