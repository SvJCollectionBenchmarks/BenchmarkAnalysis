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
//        val confIntervalsLowerCols = AnyDataColumn("Przedział ufności", confIntervals.map { "${it.lowerBound.roundToN(2)}" })
//        val confIntervalsUpperCols = AnyDataColumn("Przedział ufności", confIntervals.map { "${it.upperBound.roundToN(2)}" })
        return listOf(collections, means, sds, mins, maxs, confIntervalsCols)
    }

    fun createPolyaProfilesSummaryConfIntervals(performanceData: Map<String, List<DataColumn>>): Map<String, List<AnyDataColumn>> {
        return createPolyaProfilesSummary(performanceData) { pd ->
            pd.map { calculateConfidenceInterval(it.rows) }
                .map { "(${it.lowerBound.toInt()}, ${it.upperBound.toInt()})" }
        }
    }

    fun createPolyaProfilesSummaryAverages(performanceData: Map<String, List<DataColumn>>): Map<String, List<AnyDataColumn>> {
        return createPolyaProfilesSummary(performanceData) { pd -> pd.map { "${it.rows.average().roundToN(1)}" } }
    }

    private fun createPolyaProfilesSummary(performanceData: Map<String, List<DataColumn>>, method: (List<DataColumn>) -> List<String>): Map<String, List<AnyDataColumn>> {
        val groupsOfCollections = performanceData.toList().groupBy { (group, data) ->
            val bigLettersIndices = group.mapIndexed {idx, c -> idx to c }
                .filter { pair -> ('A' .. 'Z').contains(pair.second) }.map { it.first }
            group.substring(bigLettersIndices[1])
        }.map { it.key to it.value.toMap() }.toMap()
        val groupsOfCollectionsWithProfiles = groupsOfCollections.map { (groupOfCollections, dataForGroup) ->
            groupOfCollections to dataForGroup.toList().map { (group, data) ->
                val bigLettersIndices = group.mapIndexed {idx, c -> idx to c }
                    .filter { pair: Pair<Int, Char> -> ('A' .. 'Z').contains(pair.second) }.map { it.first }
                group.substring(0, bigLettersIndices[1]) to data }.toMap()
        }.toMap()
        return groupsOfCollectionsWithProfiles.map { (groupOfCollections, dataForGroup) ->
            val collectionsColumn = AnyDataColumn("Kolekcja", dataForGroup.values.flatten().map { it.header }.distinct())
            val profilesDataColumns = dataForGroup.map { (profileName, performanceData) ->
                AnyDataColumn(profileName, method(performanceData))
            }.toMutableList()
            profilesDataColumns.add(0, collectionsColumn)
            groupOfCollections to profilesDataColumns
        }.toMap()
    }

    fun createSingleMeasurementTable(performanceData: Map<String, List<DataColumn>>): Map<String, List<AnyDataColumn>> {
        val groups = performanceData.map { entry ->
            // TODO: Beyond stupid group name finding, please cut it in a different way
            val veryStupid = entry.key.mapIndexed { idx, c -> idx to c }.filter { it.second.isUpperCase() }
            val index = if (veryStupid.size <= 3) veryStupid[1].first else veryStupid[3].first
            entry.key.substring(0, index) to (entry.key to entry.value)
        }.groupBy { it.first }.map {
            val collectionsData = it.value.map { it.second }.map {
                // TODO: Beyond stupid group name finding, please cut it in a different way
                val veryStupid = it.first.mapIndexed { idx, c -> idx to c }.filter { it.second.isUpperCase() }
                val index = if (veryStupid.size <= 3) veryStupid[1].first else veryStupid[3].first
                it.first.substring(index) to it.second
            }.toMap()
            it.key to collectionsData
        }.toMap()
        val groupsDataColumns = groups.map { (group, operationsData) ->
            val collectionsAvgs = operationsData.map { (operation, data) ->
               operation to data.map {
                   val confInterval = calculateConfidenceInterval(it.rows)
                   it.header to "(${confInterval.lowerBound.toInt()}, ${confInterval.upperBound.toInt()})"
               }.toMap()
            }.toMap()
            val operations = AnyDataColumn("Operation", collectionsAvgs.keys.toList())
            val collections = collectionsAvgs.values.map { it.map { it.key } }.flatten().toSet()
            // TODO: THROW NULL POINTER WHEN it.value[collection] IS NULL!!!
            val columns = collections.map { collection -> AnyDataColumn(collection, collectionsAvgs.map { it.value[collection] ?: "ERROR" }) }
            group to mutableListOf(operations).apply { this.addAll(columns) }
        }.toMap()
        return groupsDataColumns
    }

    // TODO: I think currently this is working per operation, should it?
    fun createSingleProfilesSummary(performanceData: Map<String, List<DataColumn>>): Map<String, List<AnyDataColumn>> {
        val groupsOfCollections = performanceData.toList().groupBy { (group, data) ->
            val bigLettersIndices = group.mapIndexed {idx, c -> idx to c }
                .filter { pair -> ('A' .. 'Z').contains(pair.second) }.map { it.first }
            group.substring(bigLettersIndices[1])
        }.map { it.key to it.value.toMap() }.toMap()
        val groupsOfCollectionsWithProfiles = groupsOfCollections.map { (groupOfCollections, dataForGroup) ->
            groupOfCollections to dataForGroup.toList().map { (group, data) ->
                val bigLettersIndices = group.mapIndexed {idx, c -> idx to c }
                    .filter { pair: Pair<Int, Char> -> ('A' .. 'Z').contains(pair.second) }.map { it.first }
                group.substring(0, bigLettersIndices[1]) to data }.toMap()
        }.toMap()
        return groupsOfCollectionsWithProfiles.map { (groupOfCollections, dataForGroup) ->
            val collectionsColumn = AnyDataColumn("Kolekcja", dataForGroup.values.flatten().map { it.header }.distinct())
            val profilesDataColumns = dataForGroup.map { (profileName, performanceData) ->
                val confIntervals = performanceData.map { calculateConfidenceInterval(it.rows) }
                AnyDataColumn(profileName, confIntervals.map { "(${it.lowerBound.toInt()}, ${it.upperBound.toInt()})" })
            }.toMutableList()
            profilesDataColumns.add(0, collectionsColumn)
            groupOfCollections to profilesDataColumns
        }.toMap()
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

