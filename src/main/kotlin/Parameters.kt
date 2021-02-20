class Parameters {
    companion object {
        const val randomSeed = 0
        const val populationSize = 500
        const val mutationRate = 0.015
        const val pairsToMate = 300
        const val descendantsPerPair = 5

        val testData = object : TestDataParameters {
            override val childCount = 1500
        }
    }
}

interface TestDataParameters {
    val childCount: Int
}
