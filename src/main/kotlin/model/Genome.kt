package model

import utils.random
import utils.randomGene
import java.lang.Error
import kotlin.properties.Delegates

data class Genome (
    val genes: List<Int>
){
    var cost by Delegates.notNull<Double>()

    init {
        if(genes.any { it < 0 || it > 4}) throw Error("Invalid gene")
    }

    fun calcCost(units: List<DaycareUnit>, children: List<Child>){
        cost = 0.0

        val unitCapacities = units.map { it.id to it.usedCapacity }.toMap().toMutableMap()

        genes.zip(children).forEach { (g, child) ->
            cost += g

            if(g > child.ownPreferredUnits.size - 1)
                cost += 2 * child.ownPreferredUnits.size

            val unitId = child.allPreferredUnits[g].id
            unitCapacities.compute(unitId) { _, capacity -> capacity!! + child.capacity }
        }

        val capacityPercentages = unitCapacities.values.zip(units.map { it.maxCapacity })
            .map { (used, max) -> 100 * used / max }

        cost += capacityPercentages
            .map { (it - 100).coerceAtLeast(0.0) * 10 }
            .sum()
    }

    fun produceDescendant(other: Genome, units: List<DaycareUnit>, children: List<Child>): Genome {
        return this.genes.zip(other.genes)
            .map { (g1, g2) ->
                if(random.nextDouble() < Parameters.mutationRate) {
                    randomGene()
                } else {
                    if (random.nextDouble() < 0.5) g1 else g2
                }
            }
            .let { Genome(it) }
            .also { it.calcCost(units, children) }
    }

}
