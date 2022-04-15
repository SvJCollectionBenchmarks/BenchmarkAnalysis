package io

import model.AnyDataColumn
import model.DataColumn
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object FileOperations {

    fun writeDataColumnsToCSV(destPath: Path, dataColumnsMap: Map<String, List<AnyDataColumn>>, measured: String, csvSeparator: String = "; ") {
        dataColumnsMap.forEach { fileToDataColumns ->
            val (file, dataColumns) = fileToDataColumns
            writeToCSV(Paths.get(destPath.toString(), "${file}_$measured.csv"), csvSeparator, dataColumns)
        }
    }

    private fun writeToCSV(dest: Path, csvSeparator: String, dataColumns: List<AnyDataColumn>) {
        val line = "${dataColumns.joinToString(csvSeparator) { it.header }}\n"
        Files.deleteIfExists(dest)
        Files.write(dest, line.toByteArray(Charset.forName("UTF-8")), StandardOpenOption.CREATE_NEW)
        if (!dataColumns.any { it.rows.isEmpty() }) writeDataColumns(dest, csvSeparator, dataColumns)
    }

    private fun writeDataColumns(dest: Path, csvSeparator: String, dataColumns: List<AnyDataColumn>) {
        val line = "${dataColumns.joinToString(csvSeparator) { it.rows.first().toString() }}\n"
        val nextDataColumns = dataColumns.map { it.removeFirstRow() }
        Files.write(dest, line.toByteArray(Charset.forName("UTF-8")), StandardOpenOption.APPEND)
        if (!nextDataColumns.any { it.rows.isEmpty() }) writeDataColumns(dest, csvSeparator, nextDataColumns)
    }

}