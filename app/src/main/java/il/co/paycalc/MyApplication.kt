package il.co.paycalc

import android.app.Application
import il.co.paycalc.data.localDb.EventDatabase
import il.co.paycalc.data.localDb.RecordDatabase
import il.co.paycalc.data.remoteDb.RecordRemoteDataSource
import il.co.paycalc.data.repository.RecordRepository
import il.co.skystar.api.RetrofitInstance

class MyApplication : Application() {
    lateinit var recordRepository: RecordRepository
    override fun onCreate() {
        super.onCreate()

        val database = RecordDatabase.getDatabase(applicationContext)
        val remoteDataSource = RecordRemoteDataSource(RetrofitInstance.api)
        val recordDao = database?.recordDao()

        recordRepository = RecordRepository(RetrofitInstance.api, database!!, remoteDataSource, recordDao!!)
    }
}