package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allenljf.aicompanion.theme.Tokens

/**
 * 原 KKModalBottomSheetDragHandle，供 ModalBottomSheet 的 dragHandle slot 使用。
 * 畫面呼叫時一律不帶參數，故不搬 width/height/shape/color 等自訂 override（YAGNI）。
 * 原名保留（不加 App 前綴），只是移除 KK 命名。
 */
@Composable
fun DragHandle() {
    Surface(
        modifier = Modifier.padding(vertical = Tokens.spacing100),
        color = Tokens.colorBackgroundSurfaceMedium,
        shape = RoundedCornerShape(50),
    ) {
        Box(Modifier.size(width = 40.dp, height = 4.dp))
    }
}
