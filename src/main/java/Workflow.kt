import io.FileOperations
import model.AnalysisPostfix
import operations.OutcomesPlotter
import operations.OutcomesProcessor
import operations.OutcomesTransformation
import plot.rangePlot
import java.nio.file.Path
import java.nio.file.Paths

val jmhOutcomesSrcPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")
val csvOutcomesDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\csv")
val chartsDestPath: Path = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\charts")

val analysisPostfix = AnalysisPostfix.OWN

fun main() {
    try {
        val finalOutcomesSrcPath = jmhOutcomesSrcPath.add(analysisPostfix.toString().lowercase())
        val finalOutcomesDestPath = csvOutcomesDestPath.add(analysisPostfix.toString().lowercase())
        val finalChartsDestPath = chartsDestPath.add(analysisPostfix.toString().lowercase())
        val performanceDataColumnsMap = OutcomesProcessor.convertJMHPerformanceOutputToDataColumns(finalOutcomesSrcPath)
        val memoryDataColumnsMap = OutcomesProcessor.convertJMHMemoryOutputToDataColumns(finalOutcomesSrcPath)
        FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, performanceDataColumnsMap, "performance")
        FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath, memoryDataColumnsMap, "memory")
        when (analysisPostfix) {
            AnalysisPostfix.TYPES -> {
                val summaryTables = OutcomesTransformation.createPolyaProfilesSummaryAverages(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryProfiles"), summaryTables, "summaryProfiles")
            }
            AnalysisPostfix.SINGLE -> {
                val profilesDifferences = OutcomesTransformation.createSingleProfilesSummaryDifferences(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("profilesDifferences"), profilesDifferences, "profilesDifferences")
                val allProfilesSummary = OutcomesTransformation.createSingleProfilesSummaryConfIntervals(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryProfiles"), allProfilesSummary, "summaryProfiles")
                val allOperationsSummary = OutcomesTransformation.createSingleMeasurementTable(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryOperations"), allOperationsSummary, "summaryOperation")
                val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryTables"), measurementTables, "summaryTable")
            }
            AnalysisPostfix.POLYA -> {
                val summaryTables = OutcomesTransformation.createPolyaProfilesSummaryConfIntervals(performanceDataColumnsMap)
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryProfiles"), summaryTables, "summaryProfiles")
                val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryTables"), measurementTables, "summaryTable")
            }
            AnalysisPostfix.OWN -> {
                val measurementTables = performanceDataColumnsMap.map { it.key to OutcomesTransformation.createPolyaMeasurementTable(it.value) }.toMap()
                FileOperations.writeDataColumnsToCSV(finalOutcomesDestPath.add("summaryTables"), measurementTables, "summaryTable")
            }
        }
        performanceDataColumnsMap.forEach {
            val chart = OutcomesPlotter.createBoxChart(it.key, it.value)
            OutcomesPlotter.plotBoxChart(chart, finalChartsDestPath.add("${it.key}.png"))
        }
    } catch (ex: IllegalArgumentException) {
        ex.printStackTrace()
    }

}

