package utils

import java.lang.IllegalArgumentException
import kotlin.math.floor
import kotlin.math.roundToInt

fun <T> Collection<T>.pickRandom(): T {
    if(this.isEmpty()) throw IllegalArgumentException("Cannot pick from empty collection")

    val index = floor(Math.random() * this.size).roundToInt()
    return this.toList()[index]
}

fun randomGene(): Int {
    return Math.random().let {
        when {
            it < 0.005 -> 4
            it < 0.015 -> 3
            it < 0.2 -> 2
            it < 0.5 -> 1
            else -> 0
        }
    }
}
