package il.co.paycalc.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import il.co.skystar.utils.Error
import il.co.skystar.utils.Resource
import il.co.skystar.utils.Success
import kotlinx.coroutines.Dispatchers

fun <T,A> performFetchingAndSaving(localDbFetch: () -> LiveData<T>,
                                   remoteDbFetch: suspend () -> Resource<A>,
                                   localDbClear: suspend () -> Unit,
                                   localDbSave: suspend (A) -> Unit
) : LiveData<Resource<T>> =

    liveData(Dispatchers.IO) {

        emit(Resource.loading())

        val source = localDbFetch().map { Resource.success(it) }
        emitSource(source)

        val fetchResource = remoteDbFetch()

        if (fetchResource.status is Error) {
            localDbClear()
            emit(Resource.error(fetchResource.status.message))
            emitSource(source)
        } else if (fetchResource.status is Success) {
            localDbClear()
            localDbSave(fetchResource.status.data!!)
        }
    }