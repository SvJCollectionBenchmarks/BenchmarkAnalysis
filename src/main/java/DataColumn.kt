class DataColumn(val header: String, val rows: List<Double>) {
    fun removeFirstRow(): DataColumn = DataColumn(this.header, this.rows.drop(1))
}