package plot

/* Weird and stupid demo */

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun rangePlot(width: Int, height: Int, ranges: List<IntRange>, padding: Int = 40, parts: Int = 10): BufferedImage {
    val plot = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g2d = plot.createGraphics()
    g2d.drawRect(0, 0, width, height)

    val minValue = ranges.minOf { it.first }
    val maxValue = ranges.maxOf { it.last }
    val wholeDistance = maxValue - minValue

    val partDistance = wholeDistance / parts
    (0 .. parts).forEach { partIndex ->
        val x = (width - 2 * padding) * partIndex * partDistance / wholeDistance + padding
        g2d.drawString("${partIndex * partDistance + minValue}", x, 20)
        g2d.drawLine(x, 20, x, height - padding)
    }

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
    val image = rangePlot(800, 600, listOf(40 .. 60, 35 .. 70, 20 .. 50, 20 .. 70, 14 .. 51, 55 .. 68))
    ImageIO.write(image, "png", File("test.png"))
}