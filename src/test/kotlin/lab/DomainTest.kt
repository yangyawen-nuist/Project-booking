package lab

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DomainTest {
    private fun service() = BookingService(State(listOf(Equipment("scope", "Microscope"))))

    @Test
    fun `adjacent reservations are allowed but all overlap forms fail`() {
        val service = service()
        service.reserve("scope", "Alice", "2026-12-01T10:00", "2026-12-01T11:00")
        service.reserve("scope", "Bob", "2026-12-01T11:00", "2026-12-01T12:00")
        listOf("09:00" to "10:01", "10:30" to "10:45", "09:00" to "13:00", "10:00" to "11:00")
            .forEach { (start, end) ->
                assertThrows(BookingException::class.java) {
                    service.reserve("scope", "C", "2026-12-01T$start", "2026-12-01T$end")
                }
            }
        assertEquals(2, service.reservations().size)
    }

    @Test
    fun `cancel frees a slot and snapshots rebuild indexes`() {
        val service = service()
        val reservation = service.reserve("scope", "Alice", "2026-12-01T10:00", "2026-12-01T11:00")
        val restored = BookingService(service.snapshot())
        assertEquals(reservation, restored.reservations("scope").single())
        restored.cancel(reservation.id)
        assertTrue(restored.reservations().isEmpty())
        assertThrows(BookingException::class.java) { restored.cancel(reservation.id) }
    }

    @Test
    fun `invalid input is rejected`() {
        val service = service()
        assertThrows(BookingException::class.java) { service.addEquipment("scope", "Duplicate") }
        assertThrows(BookingException::class.java) { service.addEquipment("bad id", "X") }
        assertThrows(BookingException::class.java) { service.addEquipment("ok", " ") }
        assertThrows(BookingException::class.java) { service.reservations("missing") }
        assertThrows(BookingException::class.java) { parseTime("tomorrow") }
        assertThrows(BookingException::class.java) {
            service.reserve("missing", "A", "2026-12-01T10:00", "2026-12-01T11:00")
        }
        assertThrows(BookingException::class.java) {
            service.reserve("scope", " ", "2026-12-01T10:00", "2026-12-01T11:00")
        }
        assertThrows(BookingException::class.java) {
            service.reserve("scope", "A", "2026-12-01T11:00", "2026-12-01T10:00")
        }
    }

    @Test
    fun `separate equipment may share a time slot and lists stay ordered`() {
        val service = service()
        service.addEquipment("centrifuge", "Centrifuge")
        service.reserve("scope", "A", "2026-12-02T10:00", "2026-12-02T11:00")
        service.reserve("scope", "B", "2026-12-01T10:00", "2026-12-01T11:00")
        service.reserve("centrifuge", "C", "2026-12-01T10:00", "2026-12-01T11:00")
        assertEquals(listOf("centrifuge", "scope"), service.equipment().map { it.id })
        assertEquals(listOf("C", "B", "A"), service.reservations().map { it.user })
        assertEquals(2, service.reservations("scope").size)
    }

    @Test
    fun `corrupt snapshots cannot bypass domain invariants`() {
        val equipment = Equipment("x", "X")
        val reservation = Reservation("r", "x", "A", "2026-12-01T10:00", "2026-12-01T11:00")
        assertThrows(BookingException::class.java) {
            BookingService(State(listOf(equipment, equipment)))
        }
        assertThrows(BookingException::class.java) {
            BookingService(State(listOf(equipment), listOf(reservation, reservation)))
        }
        assertThrows(BookingException::class.java) {
            BookingService(State(listOf(equipment), listOf(reservation.copy(id = ""))))
        }
    }
}
