package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class OppdragslinjePlain(
    val attestert: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    val datoVedtakFom: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val datoVedtakTom: LocalDate?,
    val delytelseId: Int,
    val kodeKlasse: String,
    val linjeId: Int,
    val oppdragsId: Int,
    val sats: Double,
    val typeSats: String,
)

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: LocalDate,
    ) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}
