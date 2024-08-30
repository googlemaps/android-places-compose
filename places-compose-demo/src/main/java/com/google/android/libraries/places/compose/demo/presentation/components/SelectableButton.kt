package com.google.android.libraries.places.compose.demo.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.android.libraries.places.compose.demo.presentation.common.ButtonState

@Composable
fun SelectableButton(
    buttonState: ButtonState,
    onClick: () -> Unit,
    iconId: Int,
    contentDescription: Int,
) {
    SelectableButton(
        buttonState = buttonState,
        onClick = onClick,
    ) { tint ->
        Icon(
            painter = painterResource(iconId),
            contentDescription = stringResource(contentDescription),
            tint = tint
        )
    }
}

@Composable
fun SelectableButton(
    buttonState: ButtonState,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        val tint = when (buttonState) {
            ButtonState.NORMAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ButtonState.SELECTED -> MaterialTheme.colorScheme.primary
        }
        icon(tint)
    }
}
