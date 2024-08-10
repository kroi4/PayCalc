package il.co.paycalc

import android.app.Application
import il.co.paycalc.data.localDb.EventDatabase

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val database = EventDatabase.getDatabase(applicationContext)
        val eventDao = database?.eventDao()

//        eventRepository = EventRepository(database!!, remoteDataSource, eventDao!!)
    }
}