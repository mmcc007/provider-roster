package com.availity.spark.provider

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import java.nio.file.{Files, Paths, Path}

object ProviderRoster {


  def process(providersDF: DataFrame, visitsDF: DataFrame, outputPath: String): Unit = {
    // Create output directory if it doesn't exist
    if (!Files.exists(Paths.get(outputPath))) {
      Files.createDirectories(Paths.get(outputPath))
    }

    // Rename provider_id column in providersDF to avoid ambiguity
    val renamedProvidersDF = providersDF.withColumnRenamed("provider_id", "p_id")

    // Task 1: Total number of visits per provider
    val totalVisitsPerProviderDF = totalVisitsPerProvider(visitsDF, renamedProvidersDF)
    totalVisitsPerProviderDF.coalesce(1).write.mode("overwrite").json(s"$outputPath/total_visits_per_provider")

    // Task 2: Total number of visits per provider per month
    val visitsPerMonthDF = visitsPerProviderPerMonth(visitsDF)
    visitsPerMonthDF.coalesce(1).write.mode("overwrite").json(s"$outputPath/total_visits_per_month")

  }

  def totalVisitsPerProvider(
      visitsDF: DataFrame,
      providersDF: DataFrame
  ): DataFrame = {
    visitsDF
      .groupBy("provider_id")
      .agg(count("visit_id").alias("total_visits"))
      .join(providersDF, col("provider_id") === col("p_id"))
      .select(
        col("provider_id"),
        col("first_name"),
        col("last_name"),
        col("provider_specialty"),
        col("total_visits")
      )
  }

  def visitsPerProviderPerMonth(visitsDF: DataFrame): DataFrame = {
    visitsDF
      .withColumn("month", date_format(col("date_of_service"), "yyyy-MM"))
      .groupBy("provider_id", "month")
      .agg(count("visit_id").alias("total_visits"))
      .select("provider_id", "month", "total_visits")
  }
}
