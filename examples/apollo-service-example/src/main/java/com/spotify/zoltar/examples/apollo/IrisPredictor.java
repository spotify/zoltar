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

import com.spotify.featran.FeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Models;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.core.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.core.ModelLoader;
import com.spotify.zoltar.core.Prediction;
import com.spotify.zoltar.core.Predictor;
import com.spotify.zoltar.core.PredictorBuilder;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.metrics.Instrumentations;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.tf.JTensor;
import com.spotify.zoltar.tf.TensorFlowExtras;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;
import java.io.IOException;
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

/**
 * Iris prediction meat and potatoes.
 */
public final class IrisPredictor {

  /**
   * Configure Iris prediction, should be called at the service startup/configuration stage.
   */
  public static Predictor<Iris, Long> create(final ModelConfig modelConfig,
                                             final PredictorMetrics metrics) throws IOException {
    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final String settings = new String(Files.readAllBytes(Paths.get(modelConfig.settingsUri())));
    final ModelLoader<TensorFlowModel> modelLoader =
        Models.tensorFlow(modelConfig.modelUri().toString());

    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(irisFeatureSpec, settings);

    final TensorFlowPredictFn<Iris, Example, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Long>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn(model, vector.value()))
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    final PredictorBuilder<TensorFlowModel, Iris, Example, Long> predictorBuilder =
        Predictors
            .newBuilder(modelLoader, extractFn, predictFn)
            .with(Instrumentations.predictor(metrics));

    return predictorBuilder.predictor();
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
