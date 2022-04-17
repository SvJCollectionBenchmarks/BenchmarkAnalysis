import io.FileOperations
import model.AnalysisPostfix
import operations.OutcomesPlotter
import operations.OutcomesProcessor
import operations.OutcomesTransformation
import java.nio.file.Path
import java.nio.file.Paths

val jmhOutcomesSrcPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")
val csvOutcomesDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\csv")
val chartsDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\charts")

val analysisPostfix = AnalysisPostfix.SINGLE

fun main() {
    val finalOutcomesSrcPath = jmhOutcomesSrcPath.add(analysisPostfix.toString().lowercase())
    val finalOutcomesDestPath = csvOutcomesDestPath.add(analysisPostfix.toString().lowercase())
    val finalChartsDestPath = chartsDestPath.add(analysisPostfix.toString().lowercase())
    val performanceDataColumnsMap = OutcomesProcessor.convertJMHPerformanceOutputToDataColumns(finalOutcomesSrcPath)
    val memoryDataColumnsMap = OutcomesProcessor.convertJMHMemoryOutputToDataColumns(finalOutcomesSrcPath)
    OutcomesTransformation.createSingleMeasurementTable(performanceDataColumnsMap)
    FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, performanceDataColumnsMap, "performance")
    FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, memoryDataColumnsMap, "memory")

    when (analysisPostfix) {
        AnalysisPostfix.SINGLE -> {
            val result = OutcomesTransformation.createSingleMeasurementTable(performanceDataColumnsMap)
            FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, result, "operationSummary")
        }
        AnalysisPostfix.POLYA -> {
            val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
            FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, measurementTables, "table")
        }
        AnalysisPostfix.OWN -> {}
    }
    performanceDataColumnsMap.forEach {
        val chart = OutcomesPlotter.createBoxChart(it.key, it.value)
        OutcomesPlotter.plotBoxChart(chart)
        OutcomesPlotter.saveBoxChart(chart, finalChartsDestPath.add(chart.title))
    }
}

