package lab

import java.time.LocalDateTime
import java.util.TreeMap
import java.util.UUID

class BookingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class Equipment(val id: String, val name: String)

data class Reservation(
    val id: String,
    val equipmentId: String,
    val user: String,
    val start: String,
    val end: String,
)

data class State(
    val equipment: List<Equipment> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
)

/** Hash-based equipment lookup; a TreeMap indexes non-overlapping reservations by start time. */
class BookingService(state: State) {
    private val equipment = state.equipment.associateBy { it.id }.toMutableMap()
    private val schedules = mutableMapOf<String, TreeMap<LocalDateTime, Reservation>>()

    init {
        requireValid(equipment.size == state.equipment.size, "Duplicate equipment IDs")
        state.equipment.forEach { validateEquipment(it.id, it.name) }
        requireValid(
            state.reservations.map { it.id }.toSet().size == state.reservations.size,
            "Duplicate reservation IDs",
        )
        state.reservations.forEach { insert(it) }
    }

    fun addEquipment(id: String, name: String) {
        validateEquipment(id, name)
        requireValid(id !in equipment, "Equipment ID already exists: $id")
        equipment[id] = Equipment(id, name.trim())
    }

    fun equipment(): List<Equipment> = equipment.values.sortedBy { it.id }

    fun reserve(equipmentId: String, user: String, start: String, end: String): Reservation {
        val reservation = Reservation(UUID.randomUUID().toString(), equipmentId, user.trim(), start, end)
        insert(reservation)
        return reservation
    }

    private fun insert(reservation: Reservation) {
        requireValid(reservation.equipmentId in equipment, "Equipment not found: ${reservation.equipmentId}")
        requireValid(reservation.id.isNotBlank(), "Reservation ID must not be blank")
        requireValid(
            reservation.user.isNotBlank() && reservation.user.length <= 80,
            "User name must contain 1-80 characters",
        )
        val start = parseTime(reservation.start)
        val end = parseTime(reservation.end)
        requireValid(start < end, "End time must be after start time")
        val schedule = schedules.getOrPut(reservation.equipmentId) { TreeMap() }
        val previous = schedule.floorEntry(start)?.value
        val next = schedule.ceilingEntry(start)?.value
        requireValid(previous == null || parseTime(previous.end) <= start, "Reservation time conflict")
        requireValid(next == null || end <= parseTime(next.start), "Reservation time conflict")
        schedule[start] = reservation
    }

    fun cancel(id: String) {
        val schedule = schedules.values.firstOrNull { entries -> entries.values.any { it.id == id } }
            ?: throw BookingException("Reservation not found: $id")
        schedule.entries.removeIf { it.value.id == id }
    }

    fun reservations(equipmentId: String? = null): List<Reservation> {
        if (equipmentId != null) requireValid(equipmentId in equipment, "Equipment not found: $equipmentId")
        return schedules.filterKeys { equipmentId == null || it == equipmentId }.values
            .flatMap { it.values }.sortedWith(compareBy({ it.start }, { it.equipmentId }))
    }

    fun snapshot(): State = State(equipment(), reservations())
}

fun parseTime(value: String): LocalDateTime = try {
    LocalDateTime.parse(value)
} catch (_: java.time.format.DateTimeParseException) {
    throw BookingException("Invalid date-time; use YYYY-MM-DDTHH:mm: $value")
}

private fun validateEquipment(id: String, name: String) {
    requireValid(
        id.matches(Regex("[A-Za-z0-9_-]{1,32}")),
        "Equipment ID must be 1-32 letters, digits, underscores or hyphens",
    )
    requireValid(name.isNotBlank() && name.length <= 100, "Equipment name must contain 1-100 characters")
}

private fun requireValid(condition: Boolean, message: String) {
    if (!condition) throw BookingException(message)
}
