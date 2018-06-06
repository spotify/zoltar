/*-
 * -\-\-
 * mlengine-example
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.zoltar.examples.mlengine;

import static org.junit.Assert.assertFalse;

import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Prediction;
import java.util.List;
import org.junit.Test;
import scala.Option;

public class MlEnginePredictorExampleIT {

  @Test
  public void testPrediction() throws Exception {
    final String projectId = "data-integration-test";
    final String modelId = "iristf";
    final String versionId = "v1";

    final MlEnginePredictorExample predictor = MlEnginePredictorExample
        .create(projectId, modelId, versionId);

    final Iris input = new Iris(Option.apply(5.1),
                                Option.apply(3.5),
                                Option.apply(1.4),
                                Option.apply(0.2),
                                Option.apply("Iris-setosa"));

    final List<Prediction<Iris, Integer>> predictions = predictor
        .predict(input)
        .toCompletableFuture()
        .get();

    assertFalse(predictions.isEmpty());
  }
}
