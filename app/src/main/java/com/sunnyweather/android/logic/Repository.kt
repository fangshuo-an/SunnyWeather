package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException

object Repository {
    //自动构建并返回liveData                     所有代码运行在子线程中
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placesResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placesResponse.status == "ok") {
                val places = placesResponse.places
                Result.success((places))
            } else {
                Result.failure(RuntimeException("response status is ${placesResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)    //  将包装结果发射出去
    }
}