package com.spotify.modelserving.tf.features

import com.spotify.featran._
import com.spotify.featran.transformers._
import com.spotify.scio.bigquery._
import com.spotify.scio._
import com.spotify.scio.tensorflow._
import com.spotify.featran.scio._
import org.tensorflow.example.Example
import com.spotify.featran.tensorflow._

object IrisFeatures {

  @BigQueryType.fromTable("ml-sketchbook:model_serving.iris")
  class Iris

  def main(cmdLineArgs: Array[String]): Unit = {
    val (sc, args) = ContextAndArgs(cmdLineArgs)

    val featureSpec = FeatureSpec.of[Iris]
        .optional(_.petal_length)(Identity("petal_length"))
      .optional(_.petal_width)(Identity("petal_width"))
      .optional(_.sepal_length)(Identity("sepal_length"))
      .optional(_.sepal_width)(Identity("sepal_width"))
      .optional(_.class_name)(OneHotEncoder("class_name"))

    val data = sc.typedBigQuery[Iris]()
    val extractedFeatures = featureSpec.extract(data)
    val names = extractedFeatures.featureNames
    val features = extractedFeatures.featureValues[Example]

    features.saveAsTfExampleFile(
      args("output"),
      extractedFeatures
    )

    sc.close()
  }
}