/*-
 * -\-\-
 * zoltar-tensorflow
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

package com.spotify.modelserving.tf;

import com.google.common.collect.ImmutableMap;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.modelserving.IrisFeaturesSpec;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.FeatureExtractor;
import com.spotify.modelserving.Model.Prediction;
import com.spotify.modelserving.Model.Predictor;
import java.net.URI;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;
import scala.Option;

public class TensorFlowModelTest {

  @Test
  public void testLoad() throws Exception {
    final URI data = getClass().getResource("/iris.csv").toURI();
    final List<Iris> irisStream = Files.readAllLines(Paths.get(data))
        .stream()
        .map(l -> l.split(","))
        .map(strs -> new Iris(Option.apply(Double.parseDouble(strs[0])),
                              Option.apply(Double.parseDouble(strs[1])),
                              Option.apply(Double.parseDouble(strs[2])),
                              Option.apply(Double.parseDouble(strs[3])),
                              Option.apply(strs[4])))
        .collect(Collectors.toList());

    final Map<String, Long> classToId = ImmutableMap.of("Iris-setosa", 0L,
                                                        "Iris-versicolor", 1L,
                                                        "Iris-virginica", 2L);

    TensorFlowPredictFn<Iris, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Long>>> predictions = vectors.stream()
          .map(vector -> {
            return CompletableFuture
                .supplyAsync(() -> predict(model, vector.value()))
                .thenApply(value -> Prediction.create(vector.input(), value));
          }).collect(Collectors.toList());

      return CompletableFutures.allAsList(predictions);
    };

    final URI trainedModelUri = getClass().getResource("/trained_model").toURI();
    final URI settingsUri = getClass().getResource("/settings.json").toURI();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)),
                                             StandardCharsets.UTF_8);

    TensorFlowModel model = TensorFlowModel.create(trainedModelUri);
    FeatureExtractor<Iris, Example> irisFeatureExtractor = FeatureExtractor.create(
        IrisFeaturesSpec.irisFeaturesSpec(),
        settings,
        JFeatureSpec::extractWithSettingsExample);

    CompletableFuture<Integer> sum = Predictor
        .create(model, irisFeatureExtractor, predictFn)
        .predict(irisStream, Duration.ofMillis(1000))
        .thenApply(predictions -> {
          return predictions.stream()
              .mapToInt(prediction -> {
                String className = prediction.input().className().get();
                long value = prediction.value();

                return classToId.get(className) == value ? 1 : 0;
              }).sum();
        }).toCompletableFuture();

    Assert.assertTrue("Should be more the 0.8", sum.get() / 150f > .8);
  }

  private long predict(TensorFlowModel model, Example example) {
    // rank 1 cause we need to account for batch
    byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (Tensor<String> t = Tensors.create(b)) {
      Session.Runner runner = model.instance().session().runner()
          .feed("input_example_tensor", t)
          .fetch("linear/head/predictions/class_ids");
      List<Tensor<?>> output = runner.run();
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
