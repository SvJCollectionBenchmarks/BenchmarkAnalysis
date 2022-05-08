package plot

/* Weird and stupid demo */

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

fun rangePlot(width: Int, ranges: List<Pair<String, IntRange>>, contentPercent: Int = 80, padding: Int  = 40, partsLevel: Int = 3, barHeight: Int = 30): BufferedImage {
    val height = ranges.size * barHeight + 2 * padding
    val plot = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = plot.createGraphics()

    val minValueInRanges = ranges.map { it.second }.minOf { it.first }
    val maxValueInRanges = ranges.map { it.second }.maxOf { it.last }
    val wholeDistance = 100 * (maxValueInRanges - minValueInRanges) / contentPercent
    val difference = wholeDistance - (maxValueInRanges - minValueInRanges)
    val minValue = minValueInRanges - difference / 2
    val maxValue = maxValueInRanges + difference / 2

    g2d.color = Color.white
    g2d.fillRect(0, 0, width, height)

    g2d.color = Color.black
    g2d.drawString("$minValue", padding, 20)
    g2d.drawString("$maxValue", width - padding, 20)
    g2d.color = Color.lightGray
    g2d.drawLine(padding, 20, padding, height - padding / 2)
    g2d.drawLine(width - padding, 20, width - padding, height - padding / 2)
    var drawnRanges = mutableListOf(minValue .. maxValue)
    (0 until partsLevel).forEach {
        drawnRanges = drawnRanges.map {
            val halfValue = (it.last - it.first + 1) / 2 + it.first
            val x = (width - 2 * padding) * (halfValue - minValue) / wholeDistance + padding
            g2d.color = Color.black
            g2d.drawString("$halfValue", x, 20)
            g2d.color = Color.lightGray
            g2d.drawLine(x, 20, x, height - padding / 2)
            listOf(it.first .. halfValue, halfValue .. it.last)
        }.flatten().toMutableList()
    }

    g2d.color = Color.black
    ranges.forEachIndexed { idx, labeledRange ->
        val (label, range) = labeledRange
        val start = (width - 2 * padding) * (range.first - minValue) / wholeDistance + padding
        val end = (width - 2 * padding) * (range.last - minValue) / wholeDistance + padding
        val endY = (idx + 1) * barHeight + padding
        val startY = idx * barHeight + padding
        val currY = startY + (endY - startY) / 2
        g2d.drawString(label, start + 5, currY - 5)
        g2d.drawLine(start, currY - 5, start, currY + 5)
        g2d.drawLine(start, currY, end, currY)
        g2d.drawLine(end, currY - 5, end, currY + 5)
    }

    return plot
}

fun main() {
    val image = rangePlot(800, listOf("Collection A" to 100 .. 200, "Collection B" to  150 .. 200, "Collection C" to 120 .. 180))
    ImageIO.write(image, "png", File("test.png"))
}