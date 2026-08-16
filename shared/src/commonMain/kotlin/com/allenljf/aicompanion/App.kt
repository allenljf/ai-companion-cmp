package com.allenljf.aicompanion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.setSingletonImageLoaderFactory
import com.allenljf.aicompanion.di.appModule
import com.allenljf.aicompanion.theme.AppTheme
import com.allenljf.aicompanion.ui.AiCompanionRoot
import com.allenljf.aicompanion.ui.AiCompanionStep
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

/**
 * App 進入點：啟動獨立 Koin context（demo 只有這一個 feature，不需要跨畫面共用的全域 context）。
 * 起始頁依本地是否已有旅伴決定（loadLocalCompanion 之後看 hasLocalCompanion）；
 * null＝讀取中，顯示簡單 loading，避免尚未判定就先閃一次錯誤起始頁。
 */
@Composable
@Preview
fun App() {
    // T18：全域 Coil ImageLoader，setSingletonImageLoaderFactory 內部是 remember 過的 lazy 初始化，
    // 每次 App() 重組呼叫都安全（不會重建 ImageLoader）。
    setSingletonImageLoaderFactory { context -> companionImageLoader(context) }
    KoinApplication(application = { modules(appModule) }) {
        AppTheme {
            val viewModel = koinViewModel<AiCompanionViewModel>()
            val hasLocalCompanion by viewModel.hasLocalCompanion.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { viewModel.loadLocalCompanion() }

            val startStep = hasLocalCompanion
            if (startStep == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AiCompanionRoot(
                    viewModel = viewModel,
                    startStep = if (startStep) AiCompanionStep.Home else AiCompanionStep.CreateCompanion,
                    // demo 只有這一個 feature，沒有外層畫面可退：onFinish 保留 no-op
                    onFinish = {},
                )
            }
        }
    }
}