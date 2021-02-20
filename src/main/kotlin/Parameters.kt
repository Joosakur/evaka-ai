class Parameters {
    companion object {
        val testData = object : TestDataParameters {
            override val childCount = 1000
        }
        const val populationSize = 100
        const val mutationRate = 0.015
        const val pairsToMate = 100
        const val descendantsPerPair = 3
    }
}

interface TestDataParameters {
    val childCount: Int
}
