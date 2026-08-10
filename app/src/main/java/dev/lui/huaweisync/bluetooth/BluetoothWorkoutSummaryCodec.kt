package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.domain.WorkoutSource
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset

object BluetoothWorkoutSummaryCodec {
    private const val HEADER = "huawei-sync-workout-v1"
    private const val MAX_BYTES = 4096

    fun decode(bytes: ByteArray): DomainWorkout {
        require(bytes.size <= MAX_BYTES) { "Bluetooth workout payload is too large." }
        val lines = decodeUtf8(bytes)
            .lines()
            .map { it.removeSuffix("\r") }
            .filter { it.isNotBlank() }
        require(lines.firstOrNull() == HEADER) { "Bluetooth workout payload header is invalid." }

        val fields = linkedMapOf<String, String>()
        lines.drop(1).forEach { line ->
            val separator = line.indexOf('=')
            require(separator > 0) { "Bluetooth workout field is malformed." }
            val key = line.substring(0, separator)
            val value = line.substring(separator + 1)
            require(fields.put(key, value) == null) { "Bluetooth workout field is duplicated." }
        }

        return DomainWorkout(
            source = WorkoutSource.BLUETOOTH,
            sourceWorkoutId = fields.required("sourceRecordId"),
            title = fields["title"] ?: "Bluetooth workout",
            activityKind = fields.activityKind(),
            startTime = Instant.parse(fields.required("startTime")),
            endTime = Instant.parse(fields.required("endTime")),
            startZoneOffset = fields.optionalZoneOffset("startZoneOffset"),
            endZoneOffset = fields.optionalZoneOffset("endZoneOffset"),
            notes = fields["notes"],
            deviceName = fields["deviceName"],
        )
    }

    private fun Map<String, String>.required(key: String): String =
        requireNotNull(this[key]?.takeIf(String::isNotBlank)) { "Bluetooth workout $key is required." }

    private fun Map<String, String>.activityKind(): DomainActivityKind = when (required("activityKind")) {
        DomainActivityKind.STRENGTH_TRAINING.stableName -> DomainActivityKind.STRENGTH_TRAINING
        else -> throw IllegalArgumentException("Bluetooth workout activityKind is unsupported.")
    }

    private fun Map<String, String>.optionalZoneOffset(key: String): ZoneOffset? =
        this[key]?.takeIf(String::isNotBlank)?.let(ZoneOffset::of)

    private fun decodeUtf8(bytes: ByteArray): String = try {
        StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        throw IllegalArgumentException("Bluetooth workout payload must be valid UTF-8.")
    }
}
