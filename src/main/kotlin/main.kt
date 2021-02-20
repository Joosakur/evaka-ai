import model.Population
import model.generateTestChildren
import model.getUnitData

fun main(args: Array<String>) {
    val units = getUnitData()
    val children = generateTestChildren(Parameters.testData.childCount, units)

    println("Generated test data of ${units.size} units and ${children.size} children")

    val population = Population(units, children)

    println("Generation, Highest used capacity, Children at first preferred unit, Children at any preferred unit, Children at any preferred unit when 3 preferences given")

    for (i in 0 until 2000) {
        population.advance()

        val result = population.getBest()
        println("${population.generation}, ${result.maxCapacityPercentage}%, ${result.childrenInFirstPreferencePercentage}%, ${result.childrenInOneOfPreferencesPercentage}%, ${result.childrenInOneOfPreferencesWhen3GivenPercentage}%")
    }

}
