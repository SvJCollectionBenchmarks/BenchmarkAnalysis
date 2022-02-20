import java.nio.file.Path
import java.nio.file.Paths

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