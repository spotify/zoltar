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

import static com.google.common.base.Preconditions.checkNotNull;

import com.spotify.featran.FeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.FeatureExtractFns;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.featran.FeatranExtractFns;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;

/**
 * Helps to build a simple TensorFlow predictor.
 * In this context, a simple TensorFlow predictor has the following characteristics:
 * - Has a single input op
 * - Has a single output op
 * - Uses Featran to extract and consume Example objects
 * The output of the Predictor will always be a {@link JTensor}.
 */
public class SimpleTensorFlowPredictorBuilder<InputT> {

  private FeatureExtractFns.ExtractFn<InputT, Example> extractFn;
  private String inputOp;
  private String predictOp;
  private ModelLoader<TensorFlowModel> modelLoader;

  /**
   * Parameters for FeatranExtractFns.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings    JSON settings from a previous session.
   */
  public SimpleTensorFlowPredictorBuilder<InputT> withFeatranExtractFns(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    extractFn = FeatranExtractFns.example(featureSpec, settings);
    return this;
  }

  /**
   * Name of the input op node in the TF graph.
   */
  public SimpleTensorFlowPredictorBuilder<InputT> withInputOp(final String inputOp) {
    this.inputOp = inputOp;
    return this;
  }

  /**
   * Name of the predict op node in the TF graph.
   */
  public SimpleTensorFlowPredictorBuilder<InputT> withPredictOp(final String predictOp) {
    this.predictOp = predictOp;
    return this;
  }

  /**
   * Loads a saved TensorFlow {@link org.tensorflow.SavedModelBundle}, can be a URI to a local
   * filesystem, resource, GCS etc.
   */
  public SimpleTensorFlowPredictorBuilder<InputT> withModelUri(final String modelUri) {
    modelLoader = TensorFlowLoader.create(modelUri);
    return this;
  }

  private static JTensor predictFn(final TensorFlowModel model,
                                   final Example example,
                                   final String inputOp,
                                   final String predictOp) {
    final byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (final Tensor<String> t = Tensors.create(b)) {
      final Session.Runner runner = model.instance().session().runner()
          .feed(inputOp, t);
      final Map<String, JTensor> result = TensorFlowExtras.runAndExtract(runner, predictOp);
      return result.get(predictOp);
    }
  }

  /**
   * Builds the Predictor.
   */
  public Predictor<InputT, JTensor> build() {
    checkNotNull(extractFn);
    checkNotNull(inputOp);
    checkNotNull(predictOp);
    checkNotNull(modelLoader);

    final TensorFlowPredictFn<InputT, JTensor> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<InputT, JTensor>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn(model,
                      vector.value(),
                      inputOp,
                      predictOp))
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    return Predictor.create(modelLoader, extractFn, predictFn);

  }
}

