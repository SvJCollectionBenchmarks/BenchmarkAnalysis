package model

open class AnyDataColumn(val header: String, open val rows: List<Any>) {

    companion object {
        fun convertRowsToDataColumns(rows: List<Pair<String, List<Pair<Any, Any>>>>): List<AnyDataColumn> {
            // first().second[it].first ah yes
            var columns = rows.first().second.indices.map { AnyDataColumn(rows.first().second[it].first.toString(), listOf()) }.toMutableList()
            rows.forEach { row -> columns = row.second.mapIndexed { idx, value -> columns[idx].addRow(value.second) }.toMutableList() }
            columns.add(0, AnyDataColumn("Operacja", rows.map { it.first }))
            return columns
        }
    }

    fun removeFirstRow(): AnyDataColumn = AnyDataColumn(this.header, this.rows.drop(1).toMutableList())
    private fun addRow(value: Any) = AnyDataColumn(this.header, this.rows.toMutableList().apply { this.add(value) })
}