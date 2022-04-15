package model

open class AnyDataColumn(val header: String, open val rows: List<Any>) {
    fun removeFirstRow(): AnyDataColumn = AnyDataColumn(this.header, this.rows.drop(1))
}