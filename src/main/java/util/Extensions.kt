import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.pow
import kotlin.math.roundToInt

fun Path.add(fragment: String): Path {
    return Paths.get(this.toString(), fragment)
}

fun <T> List<T>.allPairs(): List<Pair<T, T>> {
    return (0 until this.size - 1).map { fst ->
        (fst + 1 until this.size).map { snd ->
            Pair(this[fst], this[snd])
        }
    }.flatten()
}

fun Double.roundToN(n: Int) = (this * 10.0.pow(n)).roundToInt() / 10.0.pow(n)

fun String.substringFromNthBig(n: Int): String {
    val bigLettersIndices = this.mapIndexed{idx, c -> idx to c}.filter { it.second.isUpperCase() }.map {it.first}
    return this.substring(bigLettersIndices[n])
}


fun String.substringUntilNthBig(n: Int): String {
    val bigLettersIndices = this.mapIndexed{idx, c -> idx to c}.filter { it.second.isUpperCase() }.map {it.first}
    return this.substring(0, bigLettersIndices[n])
}