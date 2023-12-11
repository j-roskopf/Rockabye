package com.joetr.bundle.ui.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.bundle.network.BehindTheNameApi
import com.joetr.bundle.network.NationalizeApi
import com.joetr.bundle.network.data.Either
import com.joetr.bundle.network.data.behind.BehindTheNameApiResponse
import com.joetr.bundle.network.data.nationalize.NationalizeApiResponse
import com.joetr.bundle.ui.data.NameYearData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NameDetailScreenModel(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val nationalizeApi: NationalizeApi,
    private val behindTheNameApi: BehindTheNameApi,
) : ScreenModel {

    private val _state = MutableStateFlow<NameDetailScreenState>(NameDetailScreenState.Loading)
    val state: StateFlow<NameDetailScreenState> = _state

    fun fetchData(data: Pair<String, List<NameYearData>>) {
        _state.value = NameDetailScreenState.Loading
        val name = data.first
        screenModelScope.launch(coroutineDispatcher) {
            // todo joer kick off work in parallel
            val unwrappedNationalityResponse = when (val nationalizeApiResponse = nationalizeApi.getNameNationality(name)) {
                is Either.Failure -> NationalizeApiResponse(
                    count = 0,
                    country = emptyList(),
                    name = name,
                )
                is Either.Success -> nationalizeApiResponse.value
            }

            val unwrappedRelatedNamesResponse = when (val relatedNames = behindTheNameApi.getRelatedNames(name)) {
                is Either.Failure -> BehindTheNameApiResponse(
                    names = emptyList(),
                )
                is Either.Success -> relatedNames.value
            }

            _state.value = NameDetailScreenState.Content(
                unwrappedNationalityResponse,
                unwrappedRelatedNamesResponse,
                data,
            )
        }
    }
}
