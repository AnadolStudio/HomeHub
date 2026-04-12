package com.anadolstudio.template.feature.common.domain

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

interface ResourceRepository {
    fun getString(@StringRes id: Int): String
    fun getColor(@ColorRes id: Int): Int
}
