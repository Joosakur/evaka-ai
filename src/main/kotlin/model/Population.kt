package model

import utils.pickRandomWithExpDistribution
import utils.randomGene
import java.util.*

class Population(
    val units: List<DaycareUnit>,
    val children: List<Child>
) {
    var generation = 0

    var members: List<Genome> = (0 until Parameters.populationSize).map { Genome(
        genes = children.indices.map { randomGene() }
    ) }.toMutableList()

    init {
        members.forEach { it.calcCost(units, children) }
    }

    fun advance() {
        generation++

        val descendants = (0 until Parameters.pairsToMate).map {
            Pair(
                members.pickRandomWithExpDistribution(2.0),
                members.pickRandomWithExpDistribution(2.0)
            )
        }.flatMap { (g1, g2) ->
            (0 until Parameters.descendantsPerPair).map { g1.produceDescendant(g2, units, children) }
        }

        members = (members + descendants)
            .sortedBy { it.cost }
            .slice(0 until Parameters.populationSize)
    }

    fun getMinimumCost() = members.minOf { it.cost }

    fun getBest() = members.minByOrNull { it.cost }!!.let { genome ->
        val resultUnits = units.map { unit ->
            val resultChildren = children
                .zip(genome.genes)
                .mapNotNull { (child, gene) ->
                    if(child.allPreferredUnits[gene].id == unit.id) child else null
                }
                .map { child -> ResultChild(
                    id = child.id,
                    capacity = child.capacity,
                    firstPreference = child.ownPreferredUnits.first().name,
                    secondPreference = child.ownPreferredUnits.getOrNull(1)?.name,
                    thirdPreference = child.ownPreferredUnits.getOrNull(2)?.name,
                    distanceToFirstPreference = child.ownPreferredUnits.first().let { first ->
                        if(first.id == unit.id) 0.0 else first.nearbyUnits.find { (id) -> unit.id == id }!!.distance
                    },
                    ownPreferenceRank = child.ownPreferredUnits.indexOfFirst { it.id == unit.id }.takeIf { it >= 0 },
                    preferenceRank = child.allPreferredUnits.indexOfFirst { it.id == unit.id }
                ) }

            val usedCapacityAfter = unit.usedCapacity + resultChildren.sumByDouble { it.capacity }

            ResultUnit(
                children = resultChildren,
                maxCapacity = unit.maxCapacity,
                usedCapacityBefore = unit.usedCapacity,
                usedCapacityAfter = usedCapacityAfter,
                capacityPercentage = 100 * usedCapacityAfter / unit.maxCapacity
            )
        }

        Result(resultUnits)
    }

}

data class Result (
    val units: List<ResultUnit>
) {
    val maxCapacityPercentage: Double
        get() = this.units.maxByOrNull { it.capacityPercentage }!!.capacityPercentage

    val minCapacityPercentage: Double
        get() = this.units.minByOrNull { it.capacityPercentage }!!.capacityPercentage

    val childrenInFirstPreferencePercentage: Double
        get() = this.units.flatMap { it.children }.let { 100.0 * it.filter { it.ownPreferenceRank == 0 }.size / it.size }

    val childrenInOneOfPreferencesPercentage: Double
        get() = this.units.flatMap { it.children }.let { 100.0 * it.filter { it.ownPreferenceRank != null }.size / it.size }
}

data class ResultUnit(
    val children: List<ResultChild>,
    val maxCapacity: Double,
    val usedCapacityBefore: Double,
    val usedCapacityAfter: Double,
    val capacityPercentage: Double
)

data class ResultChild(
    val id: UUID,
    val capacity: Double,
    val firstPreference: String,
    val secondPreference: String?,
    val thirdPreference: String?,
    val distanceToFirstPreference: Double,
    val ownPreferenceRank: Int?,
    val preferenceRank: Int
)
