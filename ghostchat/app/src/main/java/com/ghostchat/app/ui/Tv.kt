package com.ghostchat.app.ui

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** true quando l'app gira su una TV (Android TV / Google TV). */
val LocalIsTv = staticCompositionLocalOf { false }

fun isTelevision(ctx: Context): Boolean {
    val ui = ctx.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    return ui?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
        ctx.packageManager.hasSystemFeature("android.software.leanback")
}

/** Bordo ben visibile sull'elemento selezionato dal telecomando. Va messo PRIMA di clickable/focusable. */
@Composable
fun Modifier.tvFocus(shape: Shape = RoundedCornerShape(12.dp)): Modifier {
    var focused by remember { mutableStateOf(false) }
    val c = MaterialTheme.colorScheme.primary
    return this.onFocusChanged { focused = it.isFocused || it.hasFocus }
        .then(if (focused) Modifier.border(3.dp, c, shape) else Modifier)
}
