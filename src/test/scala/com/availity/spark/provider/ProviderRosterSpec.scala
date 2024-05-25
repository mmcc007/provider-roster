package com.availity.spark.provider

import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.apache.spark.sql.{SparkSession, DataFrame}

class ProviderRosterSpec
    extends AnyFunSpec
    with DataFrameComparer
    with BeforeAndAfterEach {

  var spark: SparkSession = _

  override def beforeEach(): Unit = {
    spark = SparkSession
      .builder()
      .appName("ProviderRosterTest")
      .master("local[*]")
      .getOrCreate()
  }

  override def afterEach(): Unit = {
    if (spark != null) {
      spark.stop()
    }
  }

  describe("ProviderRoster") {
    it("should process data correctly") {
      // Set up your test data
      val providersPath = "data/providers.csv"
      val visitsPath = "data/visits.csv"
      val outputPath = "target/output"

      // Read the CSV files
      val providersDF = spark.read
        .option("header", "true")
        .option("delimiter", "|")
        .csv(providersPath)
      val visitsDF = spark.read.option("header", "true").csv(visitsPath)

      // Log the schema to verify column names
      providersDF.printSchema()
      visitsDF.printSchema()

      // Call the method to test
      ProviderRoster.process(providersDF, visitsDF, outputPath)

      // Add assertions to verify the results
      // Example assertion (this depends on what your process method does)
      var resultDF: DataFrame =
        spark.read.json(s"$outputPath/total_visits_per_provider")
      resultDF.show()
      assert(resultDF.count() == 1000)

      resultDF = spark.read.json(s"$outputPath/total_visits_per_month")
      resultDF.show()
      assert(resultDF.count() == 10169)
    }
  }
}
