package model

import utils.pickRandom
import java.util.*

data class Child(
    val id: UUID,
    val ownPreferredUnits: List<DaycareUnit>,
    val allPreferredUnits: List<DaycareUnit>,
    val capacity: Double,
    val assignment: DaycareUnit? = null
)

fun generateTestChildren(count: Int, units: List<DaycareUnit>): List<Child> {
    return (0 until count).map { generateTestChild(units) }
}

private fun generateTestChild(units: List<DaycareUnit>): Child {
    val firstPreference = units.pickRandom()
    val ownPreferredUnits = mutableListOf(firstPreference)

    val r1 = Math.random()

    if(r1 < 0.7){
        val secondPreference = firstPreference.nearbyUnits.pickRandom()
            .let { (id) -> units.find { it.id == id }!! }
        ownPreferredUnits.add(secondPreference)
    }
    if(r1 < 0.4){
        val thirdPreference = firstPreference.nearbyUnits
            .filterNot { (id) -> ownPreferredUnits.any { it.id == id } }
            .pickRandom()
            .let { (id) -> units.find { it.id == id }!! }
        ownPreferredUnits.add(thirdPreference)
    }

    val allPreferredUnits = mutableListOf(*ownPreferredUnits.toTypedArray())
    while (allPreferredUnits.size < 5) {
        val extraPreference = firstPreference.nearbyUnits
            .filterNot { (id) -> allPreferredUnits.any { it.id == id } }
            .pickRandom()
            .let { (id) -> units.find { it.id == id }!! }
        allPreferredUnits.add(extraPreference)
    }

    val r2 = Math.random()
    val capacity = if (r2 < 0.05) 1.5 else if(r2 < 0.25) 0.5 else 1.0

    return Child(
        id = UUID.randomUUID(),
        ownPreferredUnits = ownPreferredUnits.toList(),
        allPreferredUnits = allPreferredUnits.toList(),
        capacity = capacity
    )
}
