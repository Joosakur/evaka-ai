package model

import utils.pickRandom
import utils.random
import java.util.*

data class Child(
    val id: UUID,
    val originalPreferredUnits: List<DaycareUnit>,
    val allPreferredUnits: List<DaycareUnit>,
    val capacity: Double
)

fun mapChild(child: ChildData, units: List<DaycareUnit>): Child {
    val originalPreferredUnits = child.preferredUnits.mapNotNull { id -> units.find { it.id == id } }

    val allPreferredUnits = mutableListOf(*originalPreferredUnits.toTypedArray())
    while (allPreferredUnits.size < 5) {
        val extraPreference = originalPreferredUnits.first().nearbyUnits
            .first { unit ->
                allPreferredUnits.none { it.id == unit.id } &&
                    originalPreferredUnits.any { it.language == unit.language } &&
                    unit.providerType != "PRIVATE_SERVICE_VOUCHER" &&
                    unit.providerType != "PRIVATE"
            }
            .let { (id) -> units.find { it.id == id }!! }

        allPreferredUnits.add(extraPreference)
    }

    val capacity = child.assistanceNeedFactor * if(child.connectedDaycare) 1.0 else 0.5

    return Child(
        id = child.id,
        originalPreferredUnits = originalPreferredUnits,
        allPreferredUnits = allPreferredUnits,
        capacity = capacity
    )
}

data class ChildData(
    val id: UUID,
    val preferredUnits: List<UUID>,
    val connectedDaycare: Boolean,
    val assistanceNeedFactor: Double
)

fun generateTestChildren(count: Int, units: List<DaycareUnit>): List<ChildData> {
    return (0 until count).map { generateTestChild(units) }
}

private fun generateTestChild(units: List<DaycareUnit>): ChildData {
    val r1 = random.nextDouble()
    val onlySwedish = r1 < 0.1
    val onlyFinnish = r1 > 0.15

    val filteredUnits = if(onlyFinnish) units.filter { it.language == "fi" } else if(onlySwedish) units.filter { it.language == "sv" } else units
    val firstPreference = filteredUnits.pickRandom()
    val originalPreferredUnits = mutableListOf(firstPreference.id)

    val filteredNearbyUnits = firstPreference.nearbyUnits.let {
        if(onlyFinnish) it.filter { it.language == "fi" } else if(onlySwedish) it.filter { it.language == "sv" } else it
    }

    val r2 = random.nextDouble()
    if(r2 < 0.7){
        val secondPreference = filteredNearbyUnits.slice(0 until 8).pickRandom().id
        originalPreferredUnits.add(secondPreference)
    }
    if(r2 < 0.4){
        val thirdPreference = firstPreference.nearbyUnits
            .slice(0 until 15)
            .filterNot { (id) -> originalPreferredUnits.contains(id) }
            .pickRandom()
            .id
        originalPreferredUnits.add(thirdPreference)
    }

    val r3 = random.nextDouble()
    return ChildData(
        id = UUID.randomUUID(),
        preferredUnits = originalPreferredUnits,
        connectedDaycare = r3 < 0.75,
        assistanceNeedFactor = if(r3 < 0.05) 1.5 else 1.0
    )
}
