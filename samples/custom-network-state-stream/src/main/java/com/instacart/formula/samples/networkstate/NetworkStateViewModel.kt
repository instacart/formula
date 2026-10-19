package com.instacart.formula.samples.networkstate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow

class NetworkStateViewModel(application: Application) : AndroidViewModel(application) {
    val outputs = NetworkStateFormula(NetworkStateStreamImpl(application)).runAsStateFlow(viewModelScope)
}
