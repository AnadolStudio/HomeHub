package com.anadolstudio.template.util

fun <E> List<E>.mapIfContains(condition: (E) -> Boolean, provideNewElement: (E) -> E): List<E> = this.map { current ->
    if (condition.invoke(current)) {
        provideNewElement.invoke(current)
    } else {
        current
    }
}
