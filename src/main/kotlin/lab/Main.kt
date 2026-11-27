package lab

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.ParentCommand
import java.nio.file.Path
import kotlin.system.exitProcess

@Command(
    name = "lab-booking", mixinStandardHelpOptions = true, version = ["1.0.0"],
    description = ["Laboratory equipment booking. Times use local laboratory time."],
    subcommands = [AddEquipment::class, ListEquipment::class, Reserve::class, ListBookings::class, Cancel::class],
)
class LabCommand : Runnable {
    @Option(names = ["--data"], scope = CommandLine.ScopeType.INHERIT, description = ["JSON data file"])
    var data: Path = Path.of("data", "bookings.json")

    override fun run() { CommandLine(this).usage(System.out) }

    fun repository(): Repository = Repository(data)
}

@Command(name = "equipment-add", mixinStandardHelpOptions = true, description = ["Add equipment"])
class AddEquipment : Runnable {
    @ParentCommand
    lateinit var parent: LabCommand

    @Option(names = ["--id"], required = true)
    lateinit var id: String

    @Option(names = ["--name"], required = true)
    lateinit var name: String

    override fun run() {
        parent.repository().transaction { it.addEquipment(id, name) }
        println("Equipment added: $id")
    }
}

@Command(name = "equipment-list", mixinStandardHelpOptions = true, description = ["List equipment"])
class ListEquipment : Runnable {
    @ParentCommand
    lateinit var parent: LabCommand

    override fun run() {
        val entries = parent.repository().transaction(false) { it.equipment() }
        if (entries.isEmpty()) println("No equipment found")
        entries.forEach { println("${it.id}\t${it.name}") }
    }
}

@Command(name = "reserve", mixinStandardHelpOptions = true, description = ["Reserve equipment for [start,end)"])
class Reserve : Runnable {
    @ParentCommand
    lateinit var parent: LabCommand

    @Option(names = ["--equipment"], required = true)
    lateinit var equipment: String

    @Option(names = ["--user"], required = true)
    lateinit var user: String

    @Option(names = ["--start"], required = true)
    lateinit var start: String

    @Option(names = ["--end"], required = true)
    lateinit var end: String

    override fun run() {
        val reservation = parent.repository().transaction { it.reserve(equipment, user, start, end) }
        println("Reservation created: ${reservation.id}")
    }
}

@Command(name = "bookings", mixinStandardHelpOptions = true, description = ["List reservations by time"])
class ListBookings : Runnable {
    @ParentCommand
    lateinit var parent: LabCommand

    @Option(names = ["--equipment"])
    var equipment: String? = null

    override fun run() {
        val entries = parent.repository().transaction(false) { it.reservations(equipment) }
        if (entries.isEmpty()) println("No reservations found")
        entries.forEach { println("${it.id}\t${it.equipmentId}\t${it.user}\t${it.start} → ${it.end}") }
    }
}

@Command(name = "cancel", mixinStandardHelpOptions = true, description = ["Cancel a reservation by ID"])
class Cancel : Runnable {
    @ParentCommand
    lateinit var parent: LabCommand

    @Option(names = ["--id"], required = true)
    lateinit var id: String

    override fun run() {
        parent.repository().transaction { it.cancel(id) }
        println("Reservation cancelled: $id")
    }
}

fun commandLine(): CommandLine = CommandLine(LabCommand()).apply {
    setExecutionExceptionHandler { error, command, _ ->
        if (error is BookingException) {
            command.err.println("Error: ${error.message}")
            1
        } else {
            command.err.println(
                "Internal error: ${error.javaClass.simpleName}; check the data or contact the maintainer",
            )
            3
        }
    }
}

@Suppress("SpreadOperator") // The picocli Java vararg API needs array spreading once at the process entry point.
fun main(args: Array<String>) { exitProcess(commandLine().execute(*args)) }
