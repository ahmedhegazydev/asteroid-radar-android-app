package com.udacity.asteroidradar.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.util.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.PictureOfDay
import com.udacity.asteroidradar.repository.AsteroidRepository
import com.udacity.asteroidradar.util.Constants
import kotlinx.coroutines.launch
import kotlin.Exception

class MainViewModel(application: Application) : AndroidViewModel(application) {
    enum class NasaApiStatus { LOADING, DONE, ERROR }

    private val database = AsteroidsDatabase.getInstance(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val startDate = getNextSevenDaysFormattedDates()[1]
    private val endDate = getNextSevenDaysFormattedDates()[6]
    private val weekEnd = getNextSevenDaysFormattedDates()[6]

    var asteroids: LiveData<List<Asteroid>> = asteroidRepository.asteroids

    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status


    private val _pictureOfTheDay = MutableLiveData<PictureOfDay>()
    val pictureOfTheDay: LiveData<PictureOfDay>
        get() = _pictureOfTheDay


    init {
        getRefreshAsteroid()
    }

    private fun getRefreshAsteroid() {
        viewModelScope.launch {
            try {
                asteroidRepository.refreshAsteroid(startDate, endDate, Constants.API_KEY)
                getPictureOfTheDay()
            } catch (_: Exception) {
                _status.value = NasaApiStatus.ERROR
            }
        }
    }

    fun getWeekAsteroid(startDate: String): LiveData<List<Asteroid>> {
        val weekAsteroid = asteroidRepository.getWeekAsteroids(startDate, weekEnd).map {
            it.asDomainModel()
        }
        return weekAsteroid
    }

    fun getTodayAsteroids(currentDate: String): LiveData<List<Asteroid>> {
        val todayAsteroids = asteroidRepository.getTodayAsteroids(currentDate).map {
            it.asDomainModel()
        }
        return todayAsteroids
    }

    private fun getPictureOfTheDay() {
        viewModelScope.launch {
            try {
                _status.value = NasaApiStatus.LOADING
                val picture = asteroidRepository.getPictureOfTheDay(Constants.API_KEY)
                picture.let {
                    _pictureOfTheDay.value = picture
                    _status.value = NasaApiStatus.DONE
                }
            } catch (e: Exception) {
                _status.value = NasaApiStatus.ERROR
            }

        }
    }
}