/*-
 * -\-\-
 * model-serving-tensorflow
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.transformers.Identity;
import com.spotify.modelserving.Model;
import com.spotify.modelserving.Model.PredictFns.PredictFn;
import com.spotify.modelserving.Model.Prediction;
import com.spotify.modelserving.Model.Predictor;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Test;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

public class TensorFlowGraphModelTest {

  private final String inputOpName = "input";
  private final String mulResult = "multiply";

  /**
   * Creates a simple TensorFlow graph that multiplies Double on input by 2.0, result is available
   * via multiply operation.
   */
  private Path createADummyTFGraph() throws IOException {
    Path graphFile;
    try (
        Graph graph = new Graph();
        Tensor<Double> t = Tensors.create(2.0D)
    ) {
      Output<Double> input = graph.opBuilder("Placeholder", inputOpName)
          .setAttr("dtype", t.dataType())
          .build().output(0);

      Output<Double> two = graph.opBuilder("Const", "two")
          .setAttr("dtype", t.dataType())
          .setAttr("value", t).build().output(0);

      graph.opBuilder("Mul", mulResult)
          .addInput(two)
          .addInput(input).build();

      graphFile = Files.createTempFile("tf-graph", ".bin");
      Files.write(graphFile, graph.toGraphDef());
    }
    return graphFile;
  }

  @Test
  public void testDummyLoadOfTensorFlowGraph() throws Exception {
    Path graphFile = createADummyTFGraph();
    try (Graph newGraph = new Graph();
        Tensor<Double> double3 = Tensors.create(3.0D)) {
      newGraph.importGraphDef(Files.readAllBytes(graphFile));
      try (Session session = new Session(newGraph)) {
        List<Tensor<?>> result = null;
        try {
          result = session.runner()
              .fetch(mulResult)
              .feed(inputOpName, double3)
              .run();
          assertEquals(result.get(0).doubleValue(), 6.0D, Double.MIN_VALUE);
        } finally {
          if (result != null) {
            result.forEach(Tensor::close);
          }
        }
      }
    }
  }

  @Test
  public void testModelInference() throws Exception {
    Path graphFile = createADummyTFGraph();
    JFeatureSpec<Double> featureSpec = JFeatureSpec.<Double>create()
        .required(d -> d, Identity.apply("feature"));
    URI settings = getClass().getResource("/settings_dummy.json").toURI();

    TensorFlowGraphModel<Double> tfModel = TensorFlowGraphModel.from(
        graphFile.toUri(),
        null,
        null,
        settings,
        featureSpec);

    PredictFn<TensorFlowGraphModel<Double>, Double, double[], Double> predictFn =
        (model, vectors) -> vectors.stream()
            .map(vector -> {
              try (Tensor<Double> input = Tensors.create(vector.value()[0])) {
                List<Tensor<?>> results = null;
                try {
                  results = model.instance().runner()
                      .fetch(mulResult)
                      .feed(inputOpName, input)
                      .run();
                  return Model.Prediction.create(vector.input(), results.get(0).doubleValue());
                } finally {
                  if (results != null) {
                    results.forEach(Tensor::close);
                  }
                }
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }).collect(Collectors.toList());

    List<Double> input = Arrays.asList(0.0D, 1.0D, 7.0D);
    double[] expected = input.stream().mapToDouble(d -> d * 2.0D).toArray();
    CompletableFuture<double[]> result = Predictor
        .create(tfModel, JFeatureExtractor::featureValuesDouble, predictFn)
        .predict(input)
        .thenApply(predictions -> {
          return predictions.stream()
              .mapToDouble(Prediction::value)
              .toArray();
        }).toCompletableFuture();

    assertArrayEquals(result.get(), expected, Double.MIN_VALUE);
  }
}
