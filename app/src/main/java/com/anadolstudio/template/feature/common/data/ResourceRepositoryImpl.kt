package com.anadolstudio.template.feature.common.data

import android.content.Context
import com.anadolstudio.template.feature.common.domain.ResourceRepository

class ResourceRepositoryImpl(private val context: Context) : ResourceRepository {
    override fun getString(id: Int): String = context.getString(id)
    override fun getColor(id: Int): Int = context.getColor(id)
}
