package operations

import model.DataColumn
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BoxChart
import org.knowm.xchart.BoxChartBuilder
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.style.BoxStyler
import splitByBig
import java.awt.Font
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

object OutcomesPlotter {

    fun createBoxChart(chartTitle: String, dataColumns: List<DataColumn>): BoxChart {
        val chart = BoxChartBuilder().title(chartTitle.splitByBig().joinToString(" ")).build()
        chart.styler.boxplotCalCulationMethod = BoxStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1
        chart.styler.isToolTipsEnabled = true
        chart.styler.axisTickLabelsFont = Font("", Font.BOLD, 18)
        chart.styler.axisTickPadding = 5
        dataColumns.forEach { dataColumn ->
            val header = dataColumn.header
            val shortHeader = if (header.contains("Sequence")) header.replace("Sequence", "Seq") else header
            chart.addSeries(shortHeader, dataColumn.rows.map { it.toInt() })
        }
        return chart
    }

    fun plotBoxChart(chart: BoxChart, destPath: Path) {
        val chartFrame = SwingWrapper(chart).displayChart()
        val location = chartFrame.locationOnScreen
        val image = Robot().createScreenCapture(Rectangle(location.x + 10, location.y + 32, chartFrame.width - 20, chartFrame.height - 40))
        ImageIO.write(image, "PNG", File(destPath.toString()))
    }

    fun saveBoxChart(chart: BoxChart, destPath: Path) {
        SwingWrapper(chart).displayChart().graphics
        BitmapEncoder.saveBitmap(chart, destPath.toString(), BitmapEncoder.BitmapFormat.PNG);
    }

}