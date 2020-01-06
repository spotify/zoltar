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

package com.spotify.zoltar

import java.nio.charset.Charset
import java.util.concurrent.ThreadLocalRandom

import com.spotify.scio.tensorflow.TFExampleIO
import com.spotify.scio.testing._
import com.spotify.zoltar.IrisFeaturesSpec.Record
import org.apache.commons.io.IOUtils
import org.tensorflow.example.Example

class IrisTest extends PipelineSpec {

  private val classes = Seq("iris-setosa", "iris-virginica", "iris-versicolor")

  private def rndD = ThreadLocalRandom.current().nextDouble(42.0D)

  private val input = (1 to 1000).par
    .map(
      _ =>
        Record(
          Some(rndD),
          Some(rndD),
          Some(rndD),
          Some(rndD),
          Some(classes(ThreadLocalRandom.current().nextInt(3)))
      )
    )
    .seq

  // scalastyle:off line.size.limit
  private val expectedFeatureSpec = IOUtils.toString(
    this.getClass.getResourceAsStream("/expected_feature_spec.json"),
    Charset.defaultCharset()
  )

  "IrisJob" should "work" in {
    JobTest[IrisFeaturesJob.type]
      .args("--output=out")
      .input(BigQueryIO[Record](Record.table), input)
      .output(TextIO("out/train/_tf_record_spec.json"))(
        _ should containSingleValue(expectedFeatureSpec)
      )
      .output(TextIO("out/eval/_tf_record_spec.json"))(
        _ should containSingleValue(expectedFeatureSpec)
      )
      .output(TFExampleIO("out/train"))(
        _ should satisfy[Example](_.size === 900 +- 50)
      )
      .output(TFExampleIO("out/eval"))(
        _ should satisfy[Example](_.size === 100 +- 50)
      )
      .output(TextIO("out/settings"))(_ should haveSize(1))
      .run()
    // scalastyle:on line.size.limit
  }

}
