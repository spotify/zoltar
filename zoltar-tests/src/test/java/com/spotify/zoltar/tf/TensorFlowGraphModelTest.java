/*
 * Copyright (C) 2019 Spotify AB
 *
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
 */
package com.spotify.zoltar.tf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.transformers.Identity;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.Model.Id;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.PredictorsTest;
import com.spotify.zoltar.featran.FeatranExtractFns;

public class TensorFlowGraphModelTest {

  private final String inputOpName = "input";
  private final String mulResult = "multiply";

  /**
   * Creates a simple TensorFlow graph that multiplies Double on input by 2.0, result is available
   * via multiply operation.
   */
  private Path createADummyTFGraph() throws IOException {
    final Path graphFile;
    try (final Graph graph = new Graph();
        final Tensor<Double> t = Tensors.create(2.0D)) {
      final Output<Double> input =
          graph
              .opBuilder("Placeholder", inputOpName)
              .setAttr("dtype", t.dataType())
              .build()
              .output(0);

      final Output<Double> two =
          graph
              .opBuilder("Const", "two")
              .setAttr("dtype", t.dataType())
              .setAttr("value", t)
              .build()
              .output(0);

      graph.opBuilder("Mul", mulResult).addInput(two).addInput(input).build();

      graphFile = Files.createTempFile("tf-graph", ".bin");
      Files.write(graphFile, graph.toGraphDef());
    }
    return graphFile;
  }

  @Test
  public void testDefaultId() throws IOException, ExecutionException, InterruptedException {
    final Path graphFile = createADummyTFGraph();
    final ModelLoader<TensorFlowGraphModel> model =
        TensorFlowGraphLoader.create(graphFile.toString(), null, null);

    final TensorFlowGraphModel tensorFlowModel = model.get().toCompletableFuture().get();

    assertThat(tensorFlowModel.id().value(), is("tensorflow-graph"));
  }

  @Test
  public void testCustomId() throws IOException, ExecutionException, InterruptedException {
    final Path graphFile = createADummyTFGraph();
    final ModelLoader<TensorFlowGraphModel> model =
        TensorFlowGraphLoader.create(Id.create("dummy"), graphFile.toString(), null, null);

    final TensorFlowGraphModel tensorFlowModel = model.get().toCompletableFuture().get();

    assertThat(tensorFlowModel.id().value(), is("dummy"));
  }

  @Test
  public void testDummyLoadOfTensorFlowGraph() throws Exception {
    final Path graphFile = createADummyTFGraph();
    try (final TensorFlowGraphModel model =
            TensorFlowGraphModel.create(graphFile.toUri(), null, null);
        final Session session = model.instance();
        final Tensor<Double> double3 = Tensors.create(3.0D)) {
      List<Tensor<?>> result = null;
      try {
        result = session.runner().fetch(mulResult).feed(inputOpName, double3).run();
        assertEquals(result.get(0).doubleValue(), 6.0D, Double.MIN_VALUE);
      } finally {
        if (result != null) {
          result.forEach(Tensor::close);
        }
      }
    }
  }

  @Test
  public void testDummyLoadOfTensorFlowGraphWithPrefix() throws Exception {
    final String prefix = "test";
    final Path graphFile = createADummyTFGraph();
    try (final TensorFlowGraphModel model =
            TensorFlowGraphModel.create(graphFile.toUri(), null, prefix);
        final Session session = model.instance();
        final Tensor<Double> double3 = Tensors.create(3.0D)) {
      List<Tensor<?>> result = null;
      try {
        result =
            session
                .runner()
                .fetch(prefix + "/" + mulResult)
                .feed(prefix + "/" + inputOpName, double3)
                .run();
        assertEquals(result.get(0).doubleValue(), 6.0D, Double.MIN_VALUE);
      } finally {
        if (result != null) {
          result.forEach(Tensor::close);
        }
      }
    }
  }

  @Test
  public void testModelInference() throws Exception {
    final Path graphFile = createADummyTFGraph();
    final JFeatureSpec<Double> featureSpec =
        JFeatureSpec.<Double>create().required(d -> d, Identity.apply("feature"));
    final URI settingsUri = getClass().getResource("/settings_dummy.json").toURI();
    final String settings =
        new String(Files.readAllBytes(Paths.get(settingsUri)), StandardCharsets.UTF_8);

    final ModelLoader<TensorFlowGraphModel> tfModel =
        TensorFlowGraphLoader.create(graphFile.toString(), null, null);

    final PredictFn<TensorFlowGraphModel, Double, double[], Double> predictFn =
        (model, vectors) ->
            vectors
                .stream()
                .map(
                    vector -> {
                      try (Tensor<Double> input = Tensors.create(vector.value()[0])) {
                        List<Tensor<?>> results = null;
                        try {
                          results =
                              model
                                  .instance()
                                  .runner()
                                  .fetch(mulResult)
                                  .feed(inputOpName, input)
                                  .run();
                          return Prediction.create(vector.input(), results.get(0).doubleValue());
                        } finally {
                          if (results != null) {
                            results.forEach(Tensor::close);
                          }
                        }
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    })
                .collect(Collectors.toList());
    final ExtractFn<Double, double[]> extractFn = FeatranExtractFns.doubles(featureSpec, settings);

    final Double[] input = new Double[] {0.0D, 1.0D, 7.0D};
    final double[] expected = Arrays.stream(input).mapToDouble(d -> d * 2.0D).toArray();
    final CompletableFuture<double[]> result =
        PredictorsTest.newBuilder(tfModel, extractFn, predictFn)
            .predictor()
            .predict(input)
            .thenApply(
                predictions -> {
                  return predictions.stream().mapToDouble(Prediction::value).toArray();
                })
            .toCompletableFuture();

    assertArrayEquals(result.get(), expected, Double.MIN_VALUE);
  }
}
