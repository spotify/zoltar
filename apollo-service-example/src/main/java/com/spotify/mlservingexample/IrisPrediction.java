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
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.Predictor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import scala.Option;

public class IrisPrediction {

  private static Predictor<Iris, Long> predictor;
  private static Map<Integer, String> idToClass = ImmutableMap.of(
      0, "Iris-setosa",
      1, "Iris-versicolor",
      2, "Iris-virginica");

  public static void setPredictor(Predictor<Iris, Long> loadedPredictor) {
    predictor = loadedPredictor;
  }

  public static Response<String> predict(final String testData) {

    if (testData == null) {
      return Response.forStatus(Status.BAD_REQUEST);
    }
    final String[] testFeatures = testData.split("-");
    Iris featureData = new Iris(
        Option.apply(Double.parseDouble(testFeatures[0])),
        Option.apply(Double.parseDouble(testFeatures[1])),
        Option.apply(Double.parseDouble(testFeatures[2])),
        Option.apply(Double.parseDouble(testFeatures[3])),
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
    }
    String predictedClass = idToClass.get(predictions[0]);
    return Response.forPayload(predictedClass);
  }
}
