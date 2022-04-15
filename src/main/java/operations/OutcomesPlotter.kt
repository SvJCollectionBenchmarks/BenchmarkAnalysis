package operations

import model.DataColumn
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BoxChart
import org.knowm.xchart.BoxChartBuilder
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.style.BoxStyler
import java.nio.file.Path

object OutcomesPlotter {

    fun createBoxChart(chartTitle: String, dataColumns: List<DataColumn>): BoxChart {
        val chart = BoxChartBuilder().title(chartTitle).build()
        chart.styler.boxplotCalCulationMethod = BoxStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1
        chart.styler.isToolTipsEnabled = true
        dataColumns.forEach { dataColumn -> chart.addSeries(dataColumn.header, dataColumn.rows.map { it.toInt() }) }
        return chart
    }

    fun plotBoxChart(chart: BoxChart) {
        SwingWrapper(chart).displayChart()
    }

    fun saveBoxChart(chart: BoxChart, destPath: Path) {
        BitmapEncoder.saveBitmap(chart, destPath.toString(), BitmapEncoder.BitmapFormat.PNG);
    }

}