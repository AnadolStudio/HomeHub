package com.anadolstudio.template.util

fun <E> Collection<E>.mapIfContains(condition: (E) -> Boolean, provideNewElement: (E) -> E): List<E> =
        this.map { current ->
            if (condition.invoke(current)) {
                provideNewElement.invoke(current)
            } else {
                current
            }
        }

fun <E> Sequence<E>.mapIfContains(condition: (E) -> Boolean, provideNewElement: (E) -> E): Sequence<E> =
        this.map { current ->
            if (condition.invoke(current)) {
                provideNewElement.invoke(current)
            } else {
                current
            }
        }
