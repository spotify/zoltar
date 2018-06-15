/*-
 * -\-\-
 * zoltar-api
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

package com.spotify.zoltar;

import com.spotify.featran.FeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.metrics.Instrumentations;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.tf.JTensor;
import com.spotify.zoltar.tf.TensorFlowExtras;
import com.spotify.zoltar.tf.TensorFlowLoader;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;

/**
 * Collection of static methods to build common TensorFlow {@link Predictors}.
 */
public class TensorFlowPredictors {

  /**
   * Builds a predictor using the Featran -> tf.Example -> TensorFlow stack.
   * In particular, your setup should be:
   * * Featran used for feature transformation
   * * tf.Example format used as input to the model
   * * TensorFlow used for the model
   * * Consumes 1 input point and returns 1 output value
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param featureSetting JSON settings from a previous session.
   * @param modelUri should point to a directory of the saved TensorFlow
   * @param outTensorExtractor Function to extract the output value from a JTensor
   * @param metrics predictor statistics to be collected
   * @param fetchOps operations to fetch.
   * @return
   */
  public static <InputT, ValueT> Predictor<InputT, ValueT> simpleFeatranPredictor(
      final FeatureSpec<InputT> featureSpec,
      final String featureSetting,
      final String modelUri,
      final Function<JTensor, ValueT> outTensorExtractor,
      final PredictorMetrics metrics,
      final String fetchOps) {

    final FeatureExtractFns.ExtractFn<InputT, Example> extractFn =
        FeatranExtractFns.example(featureSpec, featureSetting);

    final ModelLoader<TensorFlowModel> modelLoader = TensorFlowLoader.create(modelUri);

    final TensorFlowPredictFn<InputT, Example, ValueT> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<InputT, ValueT>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn(model, vector.value(), outTensorExtractor, fetchOps))
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    final PredictorBuilder<TensorFlowModel, InputT, Example, ValueT> predictorBuilder =
        Predictors
            .newBuilder(modelLoader, extractFn, predictFn)
            .with(Instrumentations.predictor(metrics));

    return predictorBuilder.predictor();
  }

  private static <ValueT> ValueT predictFn(final TensorFlowModel model,
                                           final Example example,
                                           final Function<JTensor, ValueT> outTensorExtractor,
                                           final String fetchOps) {
    final byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (final Tensor<String> t = Tensors.create(b)) {
      final Session.Runner runner = model.instance().session().runner()
          .feed("input_example_tensor", t);
      final Map<String, JTensor> result = TensorFlowExtras.runAndExtract(runner, fetchOps);
      return outTensorExtractor.apply(result.get(fetchOps));
    }
  }

}
