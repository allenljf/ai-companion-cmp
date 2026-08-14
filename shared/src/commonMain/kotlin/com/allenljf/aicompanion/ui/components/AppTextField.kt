package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.allenljf.aicompanion.theme.Tokens

/**
 * 輕量版 KKTextField——畫面只用到單一輸入框（旅伴命名），依 YAGNI 只支援
 * text / placeholder / 字數上限 / next-or-done ime action / 文字變化回呼。
 * 原本的 leadingIcon/trailingIcon/isError/formMessage/密碼可見切換等分支畫面沒用到，不搬。
 *
 * placeholderTextStringType 原為 StringType（Android Parcelable 的 i18n 包裝，未移植），
 * 改用純 String（demo 硬編字串可接受，i18n 待補）。
 */
@Composable
fun AppTextField(
    text: MutableState<String>,
    placeholder: String = "", // TODO: i18n
    textLengthLimit: Int = -1,
    isNextAction: Boolean = true,
    textListener: (String) -> Unit = {},
) {
    OutlinedTextField(
        value = text.value,
        onValueChange = { newValue ->
            val limited = if (textLengthLimit == -1) newValue else newValue.take(textLengthLimit)
            text.value = limited
            textListener(limited)
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Tokens.colorTextLight, fontSize = Tokens.fontSize3) },
        singleLine = true,
        shape = RoundedCornerShape(Tokens.radiusMd),
        keyboardOptions = KeyboardOptions(imeAction = if (isNextAction) ImeAction.Next else ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Tokens.colorTextDarker,
            unfocusedTextColor = Tokens.colorTextDarker,
            focusedBorderColor = Tokens.colorTextDarker,
            unfocusedBorderColor = Tokens.colorBorderLight,
        ),
    )
}
