package operations

import model.DataColumn
import org.apache.commons.math3.stat.StatUtils
import java.nio.file.Files
import java.nio.file.Path

object OutcomesProcessor {

    fun convertJMHPerformanceOutputToDataColumns(srcPath: Path): Map<String, List<DataColumn>> {
        return Files.list(srcPath).toList()
            .map { path ->
                val lines = path.toAbsolutePath().toFile().readLines().map { it.filter { it >= 32.toChar() }}
                    .filter { line -> line.trim().startsWith("Iteration") || line.trim().startsWith("# Benchmark:") }
                path.fileName.toString().substringBeforeLast('.') to lines
            }
            .associate { fileLinesPair ->
                val (file, lines) = fileLinesPair
                val headers = lines.mapIndexed { index, line ->
                    if (line.trim().startsWith("#"))
                        index to line.substringAfterLast('.').trim()
                    else null
                }.filterNotNull()
                file to headers.indices.map { index ->
                    val until = if (headers.lastIndex == index) lines.size else headers[index + 1].first
                    val resultsList = lines.subList(headers[index].first + 1, until)
                        .map { it.substringAfterLast(':').substringBeforeLast('o') }
                        .map { it.replace(",", ".").trim().toDouble().toInt().toDouble() }
                    DataColumn(headers[index].second, resultsList)
                }
            }
    }

    fun convertJMHMemoryOutputToDataColumns(srcPath: Path): Map<String, List<DataColumn>> {
        return Files.list(srcPath).toList()
            .map { path ->
                val lines = path.toAbsolutePath().toFile().readLines().map { it.filter { it >= 32.toChar() }}
                    .filter { line -> line.trim().startsWith("gc.alloc.rate.norm") || line.trim().startsWith("# Benchmark:")  }
                path.fileName.toString().substringBeforeLast('.') to lines
            }
            .associate { fileLinesPair ->
                val (file, lines) = fileLinesPair
                val headers = lines.mapIndexed { index, line ->
                    if (line.trim().startsWith("#"))
                        index to line.substringAfterLast('.').trim()
                    else null
                }.filterNotNull()
                file to headers.indices.map { index ->
                    val until = if (headers.lastIndex == index) lines.size else headers[index + 1].first
                    val resultsList = lines.subList(headers[index].first + 1, until)
                        .map { it.substringAfterLast(':').substringBeforeLast('B') }
                        .map { it.replace(",", ".").trim().toDouble().toInt().toDouble() }
                    DataColumn(headers[index].second, resultsList)
                }
            }
    }

    fun normalizeDataColumns(dataColumns: List<DataColumn>): List<DataColumn> {
        var lastIndex = 0
        val ranges = dataColumns.map {
            lastIndex += it.rows.size
            lastIndex - it.rows.size until lastIndex
        }
        val normalizedSamples = StatUtils.normalize(dataColumns.map { it.rows }.flatten().toDoubleArray()).toList()
        return dataColumns.mapIndexed { idx, column -> DataColumn(column.header, normalizedSamples.subList(ranges[idx].first, ranges[idx].last + 1)) }
    }

}