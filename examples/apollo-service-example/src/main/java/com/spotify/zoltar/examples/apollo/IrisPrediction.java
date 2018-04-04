/*-
 * -\-\-
 * apollo-service-example
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

package com.spotify.zoltar.examples.apollo;

import com.google.common.collect.ImmutableMap;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.featran.FeatureSpec;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.tf.JTensor;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import scala.Option;

/**
 * Iris prediction meat and potatoes.
 */
public class IrisPrediction {

  private static Predictor<Iris, JTensor> predictor;
  private static Map<Long, String> idToClass = ImmutableMap.of(
      0L, "Iris-setosa",
      1L, "Iris-versicolor",
      2L, "Iris-virginica");

  /**
   * Configure Iris prediction, should be called at the service startup/configuration stage.
   *
   * @param modelDirUri URI to the TensorFlow model directory
   * @param settingsUri URI to the settings files for Featran
   */
  public static void configure(final URI modelDirUri, final URI settingsUri) throws IOException {
    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)));

    predictor = Predictors.<Iris>simpleTensorFlow()
        .withFeatranExtractFns(irisFeatureSpec, settings)
        .withModelUri(modelDirUri.toString())
        .withInputOp("input_example_tensor")
        .withPredictOp("linear/head/predictions/class_ids")
        .build();
  }

  /**
   * Prediction endpoint. Takes a request in a form of a String containing iris features `-`
   * separated, and returns a response in a form of a predicted iris class.
   */
  public static Response<String> predict(final String requestFeatures) {

    if (requestFeatures == null) {
      return Response.forStatus(Status.BAD_REQUEST);
    }
    final String[] features = requestFeatures.split("-");

    if (features.length != 4) {
      return Response.forStatus(Status.BAD_REQUEST);
    }

    final Iris featureData = new Iris(
        Option.apply(Double.parseDouble(features[0])),
        Option.apply(Double.parseDouble(features[1])),
        Option.apply(Double.parseDouble(features[2])),
        Option.apply(Double.parseDouble(features[3])),
        Option.empty());

    try {
      final CompletionStage<String> futurePrediction = predictor
          .predict(featureData)
          .thenApply(p -> p
              .stream()
              .mapToLong(prediction -> prediction.value().longValue()[0]).toArray())
          .thenApply(p -> idToClass.get(p[0]));

      return Response.forPayload(futurePrediction.toCompletableFuture().get());
    } catch (final Exception e) {
      e.printStackTrace();
      return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }
  }

}
