package com.instacart.formula.samples.networkstate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class NetworkStateActivity : AppCompatActivity() {

    private val viewModel by viewModels<NetworkStateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val model by viewModel.outputs.collectAsStateWithLifecycle()
            NetworkStateScreen(model)
        }
    }
}
