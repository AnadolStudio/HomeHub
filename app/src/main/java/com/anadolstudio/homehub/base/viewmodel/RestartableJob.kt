package com.anadolstudio.homehub.base.viewmodel

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.Job

/** Делегат для Job: при присваивании нового значения отменяет предыдущий. */
class RestartableJob : ReadWriteProperty<Any?, Job?> {

    private var job: Job? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): Job? = job

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Job?) {
        job?.cancel()
        job = value
    }
}
