package com.joetr.bundle.ui.detail

import com.joetr.bundle.network.data.behind.BehindTheNameApiResponse
import com.joetr.bundle.network.data.nationalize.NationalizeApiResponse
import com.joetr.bundle.ui.data.NameYearData

sealed interface NameDetailScreenState {
    data object Loading : NameDetailScreenState
    data object Error : NameDetailScreenState
    data class Content(
        val nationalizeApiResponse: NationalizeApiResponse,
        val relatedNames: BehindTheNameApiResponse,
        val data: Pair<String, List<NameYearData>>,
    ) : NameDetailScreenState
}
