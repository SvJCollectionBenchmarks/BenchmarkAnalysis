package operations

import model.AnyDataColumn
import model.DataColumn
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.apache.commons.math3.stat.interval.ConfidenceInterval
import roundToN
import kotlin.math.pow
import kotlin.math.sqrt

object OutcomesTransformation {

    fun createPolyaMeasurementTable(performanceData: List<DataColumn>): List<AnyDataColumn> {
        val collections = AnyDataColumn("Kolekcja", performanceData.map { it.header } )
        val means = DataColumn("Średnia", performanceData.map { (it.rows.sum() / it.rows.size).roundToN(2) })
        val sds = DataColumn("Odch. std.", performanceData.map {
            val mean = it.rows.sum() / it.rows.size
            sqrt(it.rows.map { (it - mean).pow(2) }.sum() / it.rows.size).roundToN(2)
        })
        val mins = DataColumn("Min.", performanceData.map { (it.rows.minOrNull())?.roundToN(2)!! })
        val maxs = DataColumn("Maks.", performanceData.map { (it.rows.maxOrNull())?.roundToN(2)!! })
        val confIntervals = performanceData.map { calculateConfidenceInterval(it.rows) }
        val confIntervalsCols = AnyDataColumn("Przedział ufności", confIntervals.map { "(${it.lowerBound.roundToN(2)}, ${it.upperBound.roundToN(2)})" })
        return listOf(collections, means, sds, mins, maxs, confIntervalsCols)
    }

    fun createSingleMeasurementTable(performanceData: Map<String, List<DataColumn>>): Map<String, List<AnyDataColumn>> {
        val groups = performanceData.map { entry ->
            val veryStupid = entry.key.mapIndexed { idx, c -> idx to c }.filter { it.second.isUpperCase() }
            entry.key.substring(0, veryStupid[3].first) to (entry.key to entry.value)
        }.groupBy { it.first }.map {
            val collectionsData = it.value.map { it.second }.map {
                val veryStupid = it.first.mapIndexed { idx, c -> idx to c }.filter { it.second.isUpperCase() }
                it.first.substring(veryStupid[3].first) to it.second
            }.toMap()
            it.key to collectionsData
        }.toMap()
        val groupsDataColumns = groups.map { (group, operationsData) ->
            val collectionsAvgs = operationsData.map { (operation, data) ->
               operation to data.map { it.header to it.rows.average() }.toMap()
            }.toMap()
            val operations = AnyDataColumn("Operation", collectionsAvgs.keys.toList())
            val collections = collectionsAvgs.values.map { it.map { it.key } }.flatten().toSet()
            // TODO: THROW NULL POINTER WHEN it.value[collection] IS NULL!!!
            val columns = collections.map { collection -> DataColumn(collection, collectionsAvgs.map { it.value[collection] ?: 0.0 }) }
            group to mutableListOf(operations).apply { this.addAll(columns) }
        }.toMap()
        return groupsDataColumns
    }

    fun calculateConfidenceInterval(data: Collection<Double>, level: Double = 0.95): ConfidenceInterval {
        val stats = SummaryStatistics()
        data.forEach { stats.addValue(it) }
        val tDist = TDistribution((stats.n - 1).toDouble())
        val critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2)
        val ciSpan = critVal * stats.standardDeviation / Math.sqrt(stats.n.toDouble())
        val lower = stats.mean - ciSpan
        val upper = stats.mean + ciSpan
        return ConfidenceInterval(lower, upper, level)
    }

}

