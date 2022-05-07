package plot

/* Weird and stupid demo */

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun rangePlot(width: Int, ranges: List<IntRange>, padding: Int = 40, partsLevel: Int = 3): BufferedImage {
    val height = ranges.size * 20 + 2 * padding
    val plot = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = plot.createGraphics()

    val minValue = ranges.minOf { it.first }
    val maxValue = ranges.maxOf { it.last }
    val wholeDistance = maxValue - minValue

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
    ranges.forEachIndexed { idx, range ->
        val start = (width - 2 * padding) * (range.first - minValue) / wholeDistance + padding
        val end = (width - 2 * padding) * (range.last - minValue) / wholeDistance + padding
        g2d.drawLine(start, idx * 20 + 40 - 5, start, idx * 20 + 40 + 5)
        g2d.drawLine(start, idx * 20 + 40, end, idx * 20 + 40)
        g2d.drawLine(end, idx * 20 + 40 - 5, end, idx * 20 + 40 + 5)
    }

    return plot
}

fun main() {
    val image = rangePlot(800, listOf(6068 .. 6076, 5961 .. 5969, 6278 .. 6285))
    ImageIO.write(image, "png", File("test.png"))
}