package com.jongchan.androidarchi.etc.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jongchan.androidarchi.common.presentation.sdui.SDUIRootView
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl

@Composable
fun EtcPage(viewModel: EtcViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EtcPageContent(uiState = uiState)
}

@Composable
private fun EtcPageContent(uiState: EtcUIState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                if (uiState.sduiViewItems.isNullOrEmpty().not()) {
                    SDUIRootView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        items = uiState.sduiViewItems,
                    )
                }
            }
        }
    }
}
