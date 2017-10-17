import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, OneHotEncoder, VectorAssembler}
import org.apache.spark.ml.linalg.Vectors

val spark = SparkSession.builder().getOrCreate()
import spark.implicits._

val df = (spark
  .read
  .option("inferSchema", "true")
  .option("header", "true")
  .csv("mushrooms.csv")
).drop("veil-type")

var newDf = df
for (column <- df.columns) {
  newDf = (new StringIndexer()
            .setInputCol(column)
            .setOutputCol(s"${column}Encoded")
            .fit(newDf)
            .transform(newDf)
            .drop(column)
          )
}

val columnsForOneHot = newDf.drop("classEncoded").columns
for (column <- columnsForOneHot) {
  newDf = (new OneHotEncoder()
            .setInputCol(s"${column}")
            .setOutputCol(s"${column}OH")
            .transform(newDf)
            .drop(column)
          )
}

val assembledOutputColumns = newDf.drop("classEncoded").columns

newDf = (new VectorAssembler()
          .setInputCols(assembledOutputColumns)
          .setOutputCol("features")
          .transform(newDf)
          .select($"features", $"classEncoded")
        ).cache()

val classifier = (new LogisticRegression()
          .setMaxIter(30)
          .setTol(1E-6)
          .setFitIntercept(true)
          .setLabelCol("classEncoded")
          .setFeaturesCol("features")
        )

val Array(train, test) = newDf.randomSplit(Array(0.75, 0.25))

val model = classifier.fit(train)

val predictions = model.transform(test)

val evaluator = (new MulticlassClassificationEvaluator()
            .setMetricName("f1")
            .setLabelCol("classEncoded")
          )

val accuracy = evaluator.evaluate(predictions)
println(s"Test Error = ${1 - accuracy}")
