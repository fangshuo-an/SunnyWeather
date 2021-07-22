package com.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {
    //自动构建并返回liveData                     所有代码运行在子线程中
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {

        val placesResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placesResponse.status == "ok") {
            val places = placesResponse.places
            Result.success((places))
        } else {
            Result.failure(RuntimeException("response status is ${placesResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {

        coroutineScope {    //协程作用域，并发执行
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather =
                    Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun savePlace(place: Place)=PlaceDao.savePlace(place)

    fun getSavePlace()=PlaceDao.getSavedPlace()

    fun isPlaceSaved()=PlaceDao.isPlaceSaved()
}
