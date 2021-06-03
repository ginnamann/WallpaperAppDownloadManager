package com.example.downloadcoroutines.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.downloadcoroutines.App
import com.example.downloadcoroutines.enqueue
import com.example.downloadcoroutines.showToast
import com.example.downloadcoroutines.modelClasses.SpecialistsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PicsViewModel(application: Application) : AndroidViewModel(application) {

    val picsResponse by lazy { MutableLiveData<ArrayList<SpecialistsModel>>() }
    private val call by lazy { getApplication<App>().apiService }

     fun getPics(page: Int, limit: Int) {

        viewModelScope.launch(Dispatchers.IO)
        {

            call!!.getPics(page, limit).enqueue {

                onResponse = {
                    picsResponse.value = it.body()
                }

                onFailure = {
                    getApplication<App>().showToast(it!!.message.toString())
                }
            }
        }
    }
}