/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving

import com.spotify.featran._
import com.spotify.featran.scio._
import com.spotify.featran.tensorflow._
import com.spotify.featran.transformers._
import com.spotify.scio._
import com.spotify.scio.bigquery._
import com.spotify.scio.tensorflow._
import org.tensorflow.{example => tf}

object IrisFeaturesSpec {

  @BigQueryType.fromTable("ml-sketchbook:model_serving.iris")
  class Iris

  val irisFeaturesSpec: FeatureSpec[Iris] = FeatureSpec.of[Iris]
    .optional(_.petal_length)(StandardScaler("petal_length", withMean=true))
    .optional(_.petal_width)(StandardScaler("petal_width", withMean=true))
    .optional(_.sepal_length)(StandardScaler("sepal_length", withMean=true))
    .optional(_.sepal_width)(StandardScaler("sepal_width", withMean=true))

  val irisLabelSpec: FeatureSpec[Iris] = FeatureSpec.of[Iris]
    .optional(_.class_name)(OneHotEncoder("class_name"))

  val irisSpec = MultiFeatureSpec(irisFeaturesSpec, irisLabelSpec)
}

object IrisFeaturesJob {
  def main(cmdLineArgs: Array[String]): Unit = {
    import IrisFeaturesSpec._

    val (sc, args) = ContextAndArgs(cmdLineArgs)

    val data = sc.typedBigQuery[Iris]()
    val extractedFeatures = irisSpec.extract(data)

    val (train, test) = extractedFeatures
      .featureValues[tf.Example]
      .randomSplit(.9)

    train.saveAsTfExampleFile(args("output") + "/train", extractedFeatures)
    test.saveAsTfExampleFile(args("output") + "/eval", extractedFeatures)

    sc.close()
  }
}