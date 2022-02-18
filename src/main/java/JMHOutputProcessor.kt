import org.knowm.xchart.BoxChartBuilder
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.style.BoxStyler
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class DataColumn(val header: String, val rows: List<String>) {
    fun removeFirstRow(): DataColumn { return DataColumn(this.header, this.rows.drop(1)) }
}

object JMHOutputProcessor {

    fun convertFilesToCSV(srcPath: Path, destPath: Path, csvSeparator: String = "; ") {
        Files.list(srcPath).toList()
            .map { path ->
                path.fileName.toString().substringBeforeLast('.') to Files.readAllLines(path.toAbsolutePath(), Charset.forName("UTF-8"))
                    .filter { line -> line.trim().startsWith("Iteration") || line.trim().startsWith("# Benchmark:") }
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
                    DataColumn(headers[index].second, lines.subList(headers[index].first + 1, until)
                        .map { it.substringAfterLast(':') }
                        .map { it.substringBeforeLast('o') }
                        .map { it.trim() })
                }
            }
            .forEach { fileToDataColumns ->
                val (file, dataColumns) = fileToDataColumns
                writeToCSV(Paths.get(destPath.toString(), "$file.csv"), csvSeparator, dataColumns)
                val chart = BoxChartBuilder().title(file).build()
                chart.styler.boxplotCalCulationMethod = BoxStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1
                chart.styler.isToolTipsEnabled = true
                dataColumns.forEach { dataColumn -> chart.addSeries(dataColumn.header, dataColumn.rows.map{ it.replace(',', '.').toDouble()}) }
                SwingWrapper(chart).displayChart()
            }
    }

    private fun writeToCSV(dest: Path, csvSeparator: String, dataColumns: List<DataColumn>) {
        val line = "${dataColumns.joinToString(csvSeparator) { it.header }}\n"
        Files.deleteIfExists(dest)
        Files.write(dest, line.toByteArray(Charset.forName("UTF-8")), StandardOpenOption.CREATE_NEW)
        if (!dataColumns.any { it.rows.isEmpty() }) writeDataColumns(dest, csvSeparator, dataColumns)
    }

    private fun writeDataColumns(dest: Path, csvSeparator: String, dataColumns: List<DataColumn>) {
        val line = "${dataColumns.joinToString(csvSeparator) { it.rows.first() }}\n"
        val nextDataColumns = dataColumns.map { it.removeFirstRow() }
        Files.write(dest, line.toByteArray(Charset.forName("UTF-8")), StandardOpenOption.APPEND)
        if (!nextDataColumns.any { it.rows.isEmpty() }) writeDataColumns(dest, csvSeparator, nextDataColumns)
    }

}