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

package com.spotify.mlservingexample;

import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.FeatureExtractFn;
import com.spotify.modelserving.Model.Prediction;
import com.spotify.modelserving.Model.Predictor;
import com.spotify.modelserving.tf.TensorFlowModel;
import com.spotify.modelserving.tf.TensorFlowPredictFn;
import java.nio.LongBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;

public class IrisPredictor {

  public static Predictor<Iris, Long> loadPredictor(TensorFlowModel<Iris> loadedModel) {

    FeatureExtractFn<Iris, Example> featureExtractFn = JFeatureExtractor::featureValuesExample;
    TensorFlowPredictFn<Iris, Long> predictFn = (model, vectors) -> {
      return vectors.stream()
          .map(vector -> {
            Example example = vector.value();
            try {
              long predict = predictFn(model, example, 1L);
              return Prediction.create(vector.input(), predict);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }).collect(Collectors.toList());
    };
    Predictor<Iris, Long> predictor = Predictor
        .create(loadedModel, featureExtractFn, predictFn);

    return predictor;
  }

  private static long predictFn(TensorFlowModel<Iris> model, Example example, long timeoutSeconds)
      throws InterruptedException, ExecutionException, TimeoutException {

    byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (
        Tensor<String> t = Tensors.create(b);
    ) {
      Session.Runner runner = model.instance().session().runner()
          .feed("input_example_tensor", t)
          .fetch("linear/head/predictions/class_ids");

      List<Tensor<?>> output = CompletableFuture
          .supplyAsync(runner::run).get(timeoutSeconds, TimeUnit.SECONDS);

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
