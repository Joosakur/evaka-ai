package model

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import utils.random
import java.io.File
import java.util.UUID
import kotlin.math.*

data class DaycareUnit(
    val id: UUID,
    val name: String,
    val nearbyUnits: List<NearbyUnit>,
    val maxCapacity: Double,
    val usedCapacity: Double
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class NearbyUnit(
    val id: UUID,
    val distance: Double
)

fun getUnitData(): List<DaycareUnit> {
    val units = Klaxon()
        .converter(uuidConverter)
        .parseArray<JsonDaycareUnit>(File("units.json"))!!
        .filter { it.location != null && it.type.contains("PRESCHOOL") }
        .filterNot { it.name.contains("esiopetus") || it.name.contains("koulu") }

    val distances = mutableMapOf<UUID, List<Pair<UUID, Double>>>()
    units.forEachIndexed { i, u1 ->
        units.forEachIndexed { j, u2 ->
            if(j > i) {
                val distance = calcDistance(u1.location!!, u2.location!!)
                distances.merge(
                    u1.id,
                    listOf(Pair(u2.id, distance))
                ) { oldList, newList -> oldList + newList }
                distances.merge(
                    u2.id,
                    listOf(Pair(u1.id, distance))
                ) { oldList, newList -> oldList + newList }
            }
        }
    }

    return units.map { unit ->
        val maxCapacity = floor(random.nextDouble() * 36) + 15

        DaycareUnit(
            id = unit.id,
            name = unit.name,
            nearbyUnits = distances[unit.id]!!
                .sortedBy { it.second }
                .slice(0 until 10)
                .map { (id, distance) -> NearbyUnit(id, distance) },
            maxCapacity = maxCapacity,
            usedCapacity = round(((random.nextDouble() * 0.7) + 0.2) * maxCapacity)
        )
    }
}

private const val r = 6371000
private const val hc = Math.PI / 180
private fun calcDistance(from: Coordinates, to: Coordinates): Double {
    val p1 = from.lat * hc
    val p2 = to.lat * hc
    val dp = (to.lat - from.lat) * hc
    val dl = (to.lon - from.lon) * hc

    val sinDP = sin(dp / 2)
    val sinDL = sin(dl / 2)
    val a = sinDP * sinDP + cos(p1) * cos(p2) * sinDL * sinDL
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}

private data class JsonDaycareUnit(
    val id: UUID,
    val location: Coordinates?,
    val name: String,
    val type: List<String>
)

private val uuidConverter = object: Converter {
    override fun canConvert(cls: Class<*>) = cls == UUID::class.java

    override fun toJson(value: Any): String = value.toString()

    override fun fromJson(jv: JsonValue) = UUID.fromString(jv.string!!)
}
