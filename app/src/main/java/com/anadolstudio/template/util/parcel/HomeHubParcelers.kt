package com.anadolstudio.template.util.parcel

import android.os.Parcel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.parcelize.Parceler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * [Parceler] для [kotlinx.serialization.json.JsonObject] — он не Parcelable из коробки.
 * Гоняем через строку (raw JSON). Парсим лениво (только на чтение), поэтому это безопасно
 * с точки зрения производительности — JsonObject обычно используется как "сырой" payload.
 */
internal object JsonObjectParceler : Parceler<JsonObject> {

    override fun create(parcel: Parcel): JsonObject {
        val raw = parcel.readString().orEmpty()
        if (raw.isEmpty()) return JsonObject(emptyMap())
        return runCatching { Json.parseToJsonElement(raw) as? JsonObject }
                .getOrNull()
                ?: JsonObject(emptyMap())
    }

    override fun JsonObject.write(parcel: Parcel, flags: Int) {
        parcel.writeString(toString())
    }
}

/**
 * [Parceler] для [java.time.OffsetDateTime] — также не Parcelable.
 * Кодируем в ISO-8601 (то же представление, что использует HA в lastChanged/lastUpdated).
 */
internal object OffsetDateTimeParceler : Parceler<OffsetDateTime> {

    override fun create(parcel: Parcel): OffsetDateTime {
        val iso = parcel.readString().orEmpty()
        return runCatching { OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
                .getOrThrow()
    }

    override fun OffsetDateTime.write(parcel: Parcel, flags: Int) {
        parcel.writeString(format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }
}
