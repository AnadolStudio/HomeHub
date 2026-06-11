package com.anadolstudio.homehub.feature.add_device.addMatter.gms

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

// Google Home Mobile SDK требует API 27. Lint понимает @ChecksSdkIntAtLeast
// и разрешает звать @RequiresApi(27) внутри `if (isMatterCommissioningSupported())`.
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
internal fun isMatterCommissioningSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
