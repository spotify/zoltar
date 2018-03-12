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

package com.spotify.mlservingexample;

import com.google.common.collect.ImmutableMap;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.Model.FeatureExtractor;
import com.spotify.zoltar.Model.Predictor;
import com.spotify.zoltar.models.Models;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;
import java.io.IOException;
import java.net.URI;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
   * @param modelDirUri URI to the TensorFlow model directory
   * @param settingsUri URI to the settings files for Featran
   */
  public static void configure(URI modelDirUri, URI settingsUri) throws IOException {
    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)));
    final TensorFlowModel loadedModel = Models.tensorFlow(modelDirUri.toString());

    FeatureExtractor<Iris, Example> featureExtractor = FeatureExtractor
        .create(irisFeatureSpec, settings, JFeatureSpec::extractWithSettingsExample);

    TensorFlowPredictFn<Iris, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Model.Prediction<Iris, Long>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn(model, vector.value()))
                  .thenApply(v -> Model.Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    predictor = Predictor.create(loadedModel, featureExtractor, predictFn);
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

    Iris featureData = new Iris(
        Option.apply(Double.parseDouble(features[0])),
        Option.apply(Double.parseDouble(features[1])),
        Option.apply(Double.parseDouble(features[2])),
        Option.apply(Double.parseDouble(features[3])),
        Option.empty());

    List<Iris> irisStream = new ArrayList<Iris>();
    irisStream.add(featureData);

    int[] predictions = new int[0];
    try {
      predictions = predictor
          .predict(irisStream)
          .thenApply(p -> p
              .stream()
              .mapToInt(prediction -> {
                long value = prediction.value();
                return (int)value;
              }).toArray()).toCompletableFuture().get();
    } catch (Exception e) {
      e.printStackTrace();
      //TODO: what to return in case of failure here?
    }
    String predictedClass = idToClass.get(predictions[0]);
    return Response.forPayload(predictedClass);
  }

  private static long predictFn(TensorFlowModel model, Example example) {
    byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (Tensor<String> t = Tensors.create(b)) {
      List<Tensor<?>> output = model.instance().session().runner()
          .feed("input_example_tensor", t)
          .fetch("linear/head/predictions/class_ids")
          .run();
      LongBuffer incomingClassId = LongBuffer.allocate(1);
      try {
        output.get(0).writeTo(incomingClassId);
      } finally {
        output.forEach(Tensor::close);
      }
      return incomingClassId.get(0);
    }
  }
}
