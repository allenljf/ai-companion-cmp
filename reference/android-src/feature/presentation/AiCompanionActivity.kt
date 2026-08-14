package com.kkday.feature.ai_companion.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kkday.feature.ai_companion.presentation.compose.AiCompanionRoot
import com.kkday.feature.ai_companion.presentation.compose.AiCompanionStep
import com.kkday.feature.ai_companion.viewModel.AiCompanionViewModel
import com.kkday.input.AiCompanionRoutingInput
import com.kkday.library.common.util.getParcelableExtraCompat
import com.kkday.library.common.view.base.BaseComposeActivity
import com.kkday.library.common.view.base.EdgeToEdgeMode
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * AI 旅伴測驗流程宿主 Activity（Compose）。
 */
class AiCompanionActivity : BaseComposeActivity() {

    private val viewModel: AiCompanionViewModel by viewModel()

    // 沈浸式流程：內容延伸至狀態列/導覽列下方。AUTO_BOTTOM_PADDING 是對 rootView 呼叫 View.updatePadding()，
    // 但 Compose 佈局不會讀取 host View 的 padding，等於沒有效果——底部系統列 inset 一律改由各畫面自行用
    // navigationBarsPadding()/statusBarsPadding() 處理，此處僅需 CUSTOM 開啟 decorFitsSystemWindows(false)。
    override fun getEdgeToEdgeMode(): EdgeToEdgeMode = EdgeToEdgeMode.CUSTOM

    @Composable
    override fun Content() {
        // 依本地旅伴狀態（memberUuid 各自一份）決定起始頁：有旅伴→Home（開始答題）、無→CreateCompanion。
        // 只解析一次，之後的流程切換（如重新製作）由 AiCompanionRoot 內部 step 控制。
        // 例外：入口指定 startAtQuizGallery 時，略過本地旅伴狀態解析，直接以查看社群頁為起始頁。
        var startStep by remember { mutableStateOf<AiCompanionStep?>(null) }
        LaunchedEffect(Unit) {
            val input = intent.getParcelableExtraCompat<AiCompanionRoutingInput>(KEY_INPUT)
            if (input?.startAtQuizGallery == true) {
                startStep = AiCompanionStep.QuizGallery
            } else {
                viewModel.loadLocalCompanion()
                val hasCompanion = viewModel.hasLocalCompanion.filterNotNull().first()
                startStep = if (hasCompanion) AiCompanionStep.Home else AiCompanionStep.CreateCompanion
            }
        }
        MaterialTheme {
            startStep?.let { step ->
                AiCompanionRoot(
                    viewModel = viewModel,
                    startStep = step,
                    onFinish = { finish() },
                )
            }
        }
    }

    companion object {
        private const val KEY_INPUT = "ai_companion_input"

        fun createLaunchIntent(
            context: Context,
            input: AiCompanionRoutingInput = AiCompanionRoutingInput(),
            flags: Int? = null,
        ): Intent = Intent(context, AiCompanionActivity::class.java).apply {
            putExtra(KEY_INPUT, input)
            flags?.let { this.flags = it }
        }

        fun launch(
            context: Context,
            input: AiCompanionRoutingInput = AiCompanionRoutingInput(),
            flags: Int? = null,
        ) {
            context.startActivity(createLaunchIntent(context, input, flags))
        }
    }
}
