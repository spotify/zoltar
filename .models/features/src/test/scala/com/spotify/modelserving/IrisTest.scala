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

import java.util.concurrent.ThreadLocalRandom

import com.spotify.modelserving.IrisFeaturesSpec.Iris
import com.spotify.scio.tensorflow.TFExampleIO
import com.spotify.scio.testing._
import org.tensorflow.example.Example

class IrisTest extends PipelineSpec {

  private val classes = Seq("iris-setosa", "iris-virginica", "iris-versicolor")

  private def rndD = ThreadLocalRandom.current().nextDouble(42.0D)

  private val input = (1 to 10000)
    .par
    .map(_ => Iris(Some(rndD), Some(rndD), Some(rndD), Some(rndD),
      Some(classes(ThreadLocalRandom.current().nextInt(3)))))
    .seq

  // scalastyle:off line.size.limit
  private val expectedFeatureSpec =
    """{"version":1,"features":[{"name":"petal_length","kind":"FloatList","tags":{"multispec-id":"0"}},{"name":"petal_width","kind":"FloatList","tags":{"multispec-id":"0"}},{"name":"sepal_length","kind":"FloatList","tags":{"multispec-id":"0"}},{"name":"sepal_width","kind":"FloatList","tags":{"multispec-id":"0"}},{"name":"class_name_iris-setosa","kind":"FloatList","tags":{"multispec-id":"1"}},{"name":"class_name_iris-versicolor","kind":"FloatList","tags":{"multispec-id":"1"}},{"name":"class_name_iris-virginica","kind":"FloatList","tags":{"multispec-id":"1"}}],"compression":"UNCOMPRESSED"}"""
  "IrisJob" should "work" in {
    JobTest[IrisFeaturesJob.type]
      .args("--output=out")
      .input(BigQueryIO[Iris](Iris.table), input)
      .output(TextIO("out/train/_tf_record_spec.json"))(_ should containSingleValue(expectedFeatureSpec))
      .output(TextIO("out/eval/_tf_record_spec.json"))(_ should containSingleValue(expectedFeatureSpec))
      .output(TFExampleIO("out/train"))(_ should satisfy[Example](_.size === 9000+-500))
      .output(TFExampleIO("out/eval"))(_ should satisfy[Example](_.size === 1000+-500))
      .output(TextIO("out/settings"))(_ should haveSize(1))
      .run()
  // scalastyle:on line.size.limit
  }

}
