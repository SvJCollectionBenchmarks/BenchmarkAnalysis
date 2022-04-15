package model

class DataColumn(header: String, override val rows: List<Double>): AnyDataColumn(header, rows)