import org.apache.commons.math3.stat.inference.TestUtils

object OutcomesAnalyzer {

    fun performOneWayAnova(dataColumns: List<DataColumn>, alpha: Double = 0.01) {
        dataColumns.allPairs().forEach {
            val classes = listOf(it.first.rows.toDoubleArray(), it.second.rows.toDoubleArray())
            val pValue = TestUtils.oneWayAnovaPValue(classes)
            val testResult = TestUtils.oneWayAnovaTest(classes, alpha)
            println("In a (${it.first.header}, ${it.second.header} pair, the p-value is $pValue")
            println("The averages of the groups are${if (testResult) " not" else ""} equal with significance level $alpha.")
        }

    }

}