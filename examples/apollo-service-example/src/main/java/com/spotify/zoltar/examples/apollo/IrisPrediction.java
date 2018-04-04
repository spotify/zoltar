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
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.Models;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.tf.JTensor;
import com.spotify.zoltar.tf.TensorFlowExtras;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;
import scala.Option;

/**
 * Iris prediction meat and potatoes.
 */
public class IrisPrediction {

  private static Predictor<Iris, Long> predictor;
  private static Map<Integer, String> idToClass = ImmutableMap.of(
      0, "Iris-setosa",
      1, "Iris-versicolor",
      2, "Iris-virginica");

  /**
   * Configure Iris prediction, should be called at the service startup/configuration stage.
   *
   * @param modelDirUri URI to the TensorFlow model directory
   * @param settingsUri URI to the settings files for Featran
   */
  public static void configure(final URI modelDirUri, final URI settingsUri) throws IOException {
    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)));
    final ModelLoader<TensorFlowModel> modelLoader =
        Models.tensorFlow(modelDirUri.toString());

    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(irisFeatureSpec, settings);

    final TensorFlowPredictFn<Iris, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Long>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn(model, vector.value()))
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    predictor = Predictors
        .newBuilder(modelLoader, extractFn, predictFn)
        .predictor();
  }

  /**
   * Prediction endpoint. Takes a request in a from of a String containing iris features `-`
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

    int[] predictions = new int[0];
    try {
      predictions = predictor
          .predict(featureData)
          .thenApply(p -> p
              .stream()
              .mapToInt(prediction -> prediction.value().intValue())
              .toArray()).toCompletableFuture().get();
    } catch (final Exception e) {
      e.printStackTrace();
      //TODO: what to return in case of failure here?
    }
    final String predictedClass = idToClass.get(predictions[0]);
    return Response.forPayload(predictedClass);
  }

  private static long predictFn(final TensorFlowModel model, final Example example) {
    final byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (final Tensor<String> t = Tensors.create(b)) {
      final Session.Runner runner = model.instance().session().runner()
              .feed("input_example_tensor", t);
      final String op = "linear/head/predictions/class_ids";
      final Map<String, JTensor> result = TensorFlowExtras.runAndExtract(runner, op);
      return result.get(op).longValue()[0];
    }
  }
}
