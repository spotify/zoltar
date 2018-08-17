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

package com.spotify.zoltar.tf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.core.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.IrisHelper;
import com.spotify.zoltar.core.Model.Id;
import com.spotify.zoltar.core.ModelLoader;
import com.spotify.zoltar.core.Prediction;
import com.spotify.zoltar.core.Predictor;
import com.spotify.zoltar.PredictorsTest;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.tf.TensorFlowModel.Options;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;

public class TensorFlowModelTest {

  public static Predictor<Iris, Long> getTFIrisPredictor() throws Exception {
    final TensorFlowPredictFn<Iris, Example, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Long>>> predictions = vectors.stream()
          .map(vector -> CompletableFuture
              .supplyAsync(() -> predict(model, vector.value()))
              .thenApply(value -> Prediction.create(vector.input(), value)))
          .collect(Collectors.toList());

      return CompletableFutures.allAsList(predictions);
    };

    final URI trainedModelUri = TensorFlowModelTest.class.getResource("/trained_model").toURI();
    final URI settingsUri = TensorFlowModelTest.class.getResource("/settings.json").toURI();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)),
        StandardCharsets.UTF_8);

    final ModelLoader<TensorFlowModel> model = TensorFlowLoader.create(trainedModelUri.toString());

    final ExtractFn<Iris, Example> extractFn = FeatranExtractFns
        .example(IrisFeaturesSpec.irisFeaturesSpec(), settings);

    return PredictorsTest
        .newBuilder(model, extractFn, predictFn)
        .predictor();
  }

  @Test
  public void testDefaultId()
      throws URISyntaxException, ExecutionException, InterruptedException {
    final URI trainedModelUri = TensorFlowModelTest.class.getResource("/trained_model").toURI();
    final ModelLoader<TensorFlowModel> model = TensorFlowLoader.create(trainedModelUri.toString());

    final TensorFlowModel tensorFlowModel = model.get().toCompletableFuture().get();

    assertThat(tensorFlowModel.id().value(), is("tensorflow"));
  }

  @Test
  public void testCustomId() throws URISyntaxException, ExecutionException, InterruptedException {
    final URI trainedModelUri = TensorFlowModelTest.class.getResource("/trained_model").toURI();
    final ModelLoader<TensorFlowModel> model =
        TensorFlowLoader.create(Id.create("dummy"), trainedModelUri.toString());

    final TensorFlowModel tensorFlowModel = model.get().toCompletableFuture().get();

    assertThat(tensorFlowModel.id().value(), is("dummy"));
  }

  @Test
  public void testModelInference() throws Exception {
    final Iris[] irisStream = IrisHelper.getIrisTestData();

    final Map<String, Long> classToId = ImmutableMap.of("Iris-setosa", 0L,
        "Iris-versicolor", 1L,
        "Iris-virginica", 2L);

    final CompletableFuture<Integer> sum = getTFIrisPredictor()
        .predict(Duration.ofSeconds(10), irisStream)
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

  @Test
  public void optionsSerializable() throws IOException {
    final Options options = Options.builder()
        .tags(Collections.singletonList("serve"))
        .build();

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(options);
  }

  private static long predict(final TensorFlowModel model, final Example example) {
    // rank 1 cause we need to account for batch
    final byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (final Tensor<String> t = Tensors.create(b)) {
      final Session.Runner runner = model.instance().session().runner()
          .feed("input_example_tensor", t)
          .fetch("linear/head/predictions/class_ids");
      final List<Tensor<?>> output = runner.run();
      final LongBuffer incomingClassId = LongBuffer.allocate(1);

      try {
        output.get(0).writeTo(incomingClassId);
      } finally {
        output.forEach(Tensor::close);
      }
      return incomingClassId.get(0);
    }
  }
}
