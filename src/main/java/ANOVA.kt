import java.nio.file.Paths

fun main() {
    JMHOutputProcessor.convertFilesToCSV(Paths.get("C:\\Users\\wojci\\source\\master-thesis\\BenchmarkGeneration\\measurements\\raw"), Paths.get("C:\\Users\\wojci\\source\\master-thesis\\BenchmarkGeneration\\measurements\\csv"))
}