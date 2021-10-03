package com.example.downloadcoroutines.viewModel

import androidx.lifecycle.*
import com.example.downloadcoroutines.modelClasses.PicsModel
import com.example.downloadcoroutines.utils.NetworkResource
import com.example.downloadcoroutines.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PicsViewModel @Inject constructor(val picsRepository: PicsRepository) : ViewModel() {
    lateinit var picsResponse: LiveData<NetworkResource<List<PicsModel>>>

    private val _altPicsResponse by lazy { MutableLiveData<Resource<ArrayList<PicsModel>>>() }
    val altPicsResponse: LiveData<Resource<ArrayList<PicsModel>>> get() = _altPicsResponse

    fun getPics(page: Int, limit: Int) {
        picsResponse = picsRepository.getPics(page, limit).asLiveData()
    }

    fun getAltPics(page: Int, limit: Int) {
        viewModelScope.launch {
            _altPicsResponse.postValue(Resource.loading(null))
            picsRepository.getAltPics(page, limit).apply {
                collect {
                    if (it.isSuccessful)
                        _altPicsResponse.postValue(Resource.success(it.body()))
                    else
                        _altPicsResponse.postValue(
                            Resource.error(
                                it.errorBody().toString(),
                                it.body()
                            )
                        )
                }
            }
        }
    }


}