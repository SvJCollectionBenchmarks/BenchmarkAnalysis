csv_path <- "C:\\Users\\wojci\\source\\master-thesis\\measurements\\csv"

csv_files <- list.files(path = csv_path, pattern = "*.csv")
for (csv_file in csv_files) {
  csv_data <- read.csv(file.path(csv_path, csv_file), sep = ";")
  samples_count <- nrow(csv_data)
  anova_data <- data.frame(group = NULL, values = NULL)
  for(i in seq_len(ncol(csv_data))) {
    anova_strip <- data.frame(
      group = rep(names(csv_data)[i], each = samples_count),
      values = csv_data[, i]
    )
    anova_data <- rbind(anova_data, anova_strip)
  }
  anova_model <- aov(values~group, data=anova_data)
  print(summary(anova_model))
  print(TukeyHSD(anova_model, conf.level=.95))
}