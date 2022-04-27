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

val analysisPostfix = AnalysisPostfix.POLYA

fun main() {
    try {
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
                val allOperationsSummary = OutcomesTransformation.createSingleMeasurementTable(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, allOperationsSummary, "operationSummary")
                val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, measurementTables, "table")
            }
            AnalysisPostfix.POLYA -> {
                val summaryTables = OutcomesTransformation.createPolyaProfilesSummary(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, summaryTables, "profilesSummary")
                val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, measurementTables, "table")
            }
            AnalysisPostfix.OWN -> {}
        }
        performanceDataColumnsMap.forEach {
//            val profileEndIndex = it.key.substring(1).indexOfFirst { ('A'..'Z').contains(it)} + 1
//            val analysisPhase = analysisPostfix.name.lowercase().replaceFirstChar { it.uppercase() }
            val chart = OutcomesPlotter.createBoxChart(it.key, it.value)
            OutcomesPlotter.plotBoxChart(chart)
            OutcomesPlotter.saveBoxChart(chart, finalChartsDestPath.add(it.key))
        }
    } catch (ex: IllegalArgumentException) {
        ex.printStackTrace()
    }

}

