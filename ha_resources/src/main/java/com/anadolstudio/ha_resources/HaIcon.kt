package com.anadolstudio.ha_resources

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = HaIconSerializer::class)
data class HaIcon(
        val haIcon: String,
        val drawableRes: Int,
)

object HaIconSerializer : KSerializer<HaIcon> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("HaIcon", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: HaIcon) {
        encoder.encodeString(value.haIcon)
    }

    override fun deserialize(decoder: Decoder): HaIcon {
        val key = decoder.decodeString()
        val resId = HaIcons.icons[key] ?: R.drawable.ic_mdi_new_box

        return HaIcon(haIcon = key, drawableRes = resId)
    }
}
