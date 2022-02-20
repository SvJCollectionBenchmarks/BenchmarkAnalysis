import io.FileOperations
import operations.OutcomesAnalyzer
import operations.OutcomesPlotter
import operations.OutcomesProcessor
import java.nio.file.Path
import java.nio.file.Paths

val jmhOutcomesSrcPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")
val csvOutcomesDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\csv")
val chartsDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\charts")

fun main() {
    val dataColumnsMap = OutcomesProcessor.convertJMHOutputToDataColumns(jmhOutcomesSrcPath)
    FileOperations.writeDataColumnsToCSV(csvOutcomesDestPath, dataColumnsMap)
    dataColumnsMap.forEach {
        val chart = OutcomesPlotter.createBoxChart(it.key, it.value)
        OutcomesPlotter.plotBoxChart(chart)
        OutcomesPlotter.saveBoxChart(chart, chartsDestPath.add(chart.title))
    }
    dataColumnsMap.forEach { OutcomesAnalyzer.performOneWayAnova(it.value) }
}