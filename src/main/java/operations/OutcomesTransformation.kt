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

    fun createMeasurementTable(performanceData: List<DataColumn>): List<AnyDataColumn> {
        val collections = AnyDataColumn("Collection", performanceData.map { it.header } )
        val means = DataColumn("Mean", performanceData.map { (it.rows.sum() / it.rows.size).roundToN(2) })
        val sds = DataColumn("Standard deviation", performanceData.map {
            val mean = it.rows.sum() / it.rows.size
            sqrt(it.rows.map { it - mean }.sum().pow(2) / it.rows.size)
        })
        val mins = DataColumn("Minimum", performanceData.map { (it.rows.minOrNull())?.roundToN(2)!! })
        val maxs = DataColumn("Maximum", performanceData.map { (it.rows.maxOrNull())?.roundToN(2)!! })
        val confIntervals = performanceData.map { calculateConfidenceInterval(it.rows) }
        val ciLowers = DataColumn("CI lower", confIntervals.map { it.lowerBound.roundToN(2) })
        val ciUppers = DataColumn("CI upper", confIntervals.map { it.upperBound.roundToN(2) })
        return listOf(collections, means, sds, mins, maxs, ciLowers, ciUppers)
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

