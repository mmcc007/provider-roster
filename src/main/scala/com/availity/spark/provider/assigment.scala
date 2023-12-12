import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object ProviderVisits {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("ProviderVisits").getOrCreate()

    try {
	
	  // Q1
	  
	   val visitsSchema = StructType(
			Array(
			  StructField("visit_id", StringType, nullable = true),
			  StructField("provider_id", StringType, nullable = true),
			  StructField("date", DateType, nullable = true)
			)
		  )
      val providers: DataFrame = spark.read.option("header", "true").csv("provider-roster/data/providers.csv")	
	  
      val visits: DataFrame = spark.read.option("header", "true").schema(visitsSchema).csv("provider-roster/data/visits.csv")
      val totalvisit = visits.groupBy("provider_id").agg(count("visit_id").alias("totalVisits"))
      val output_Q1 = totalvisit.join(providers, "provider_id").select("provider_id", "name", "specialty", "totalVisits")
      output_Q1.write.partitionBy("specialty").json("output/totalVisitsByProvider")
	  
	  //Q2
      val all_visit_per_month = visits.withColumn("month", date_format(col("date"), "yyyy-MM"))
      val group_visit_month = all_visit_per_month.groupBy("provider_id", "month").agg(count("visit_id").alias("totalVisits"))
      group_visit_month.write.json("output/totalVisitsByProviderPerMonth")
    } finally {
      spark.stop()
    }
  }
}
