package com.anadolstudio.homehub.feature.addMatter.gms

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Google Home Mobile SDK (`Matter.getCommissioningClient(...).commissionDevice(...)`)
 * требует API 27 (Android 8.1). Mинимальный SDK всего проекта оставлен 26 —
 * чтобы на Android 8.0 не падать с `VerifyError`, всё взаимодействие с
 * Matter API закрыто этой проверкой.
 *
 * `@ChecksSdkIntAtLeast` подсказывает Android Lint: «если функция вернула true,
 * `Build.VERSION.SDK_INT >= 27`», поэтому компилятор разрешает вызывать
 * `@RequiresApi(27)`-методы внутри `if (isMatterCommissioningSupported()) { ... }`.
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
internal fun isMatterCommissioningSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
