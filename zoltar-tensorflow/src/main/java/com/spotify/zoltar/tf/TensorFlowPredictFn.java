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

import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Vector;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;

/**
 * TensorFlow flavor of {@link AsyncPredictFn} using {@link TensorFlowModel}.
 *
 * @param <InputT> type of the raw input to the feature extraction.
 * @param <VectorT> type of the feature extraction output.
 * @param <ValueT> type of the prediction result.
 */
@FunctionalInterface
public interface TensorFlowPredictFn<InputT, VectorT, ValueT>
    extends AsyncPredictFn<TensorFlowModel, InputT, VectorT, ValueT> {

  /**
   * TensorFlow Example prediction function.
   *
   * @param outTensorExtractor Function to extract the output value from a JTensor
   * @param fetchOp            operation to fetch.
   */
  static <InputT, ValueT> TensorFlowPredictFn<InputT, Example, ValueT> example(
      final Function<JTensor, ValueT> outTensorExtractor,
      final String fetchOp) {
    return (model, vectors) -> {
      final List<Vector<InputT, List<Example>>> bulk = vectors.stream()
          .map(vector -> {
            final List<Example> examples = Collections.singletonList(vector.value());
            return Vector.create(vector.input(), examples);
          }).collect(Collectors.toList());

      return TensorFlowPredictFn
          .<InputT, ValueT>exampleBatch(outTensorExtractor, fetchOp)
          .apply(model, bulk);
    };
  }

  /**
   * TensorFlow Example prediction function.
   *
   * @param outTensorExtractor Function to extract the output value from a JTensor
   * @param fetchOp           operation to fetch.
   */
  static <InputT, ValueT> TensorFlowPredictFn<InputT, List<Example>, ValueT> exampleBatch(
      final Function<JTensor, ValueT> outTensorExtractor,
      final String fetchOp) {

    final BiFunction<TensorFlowModel, List<Example>, ValueT> predictFn = (model, examples) -> {
      final byte[][] bytes = examples.stream()
          .map(Example::toByteArray)
          .toArray(byte[][]::new);

      try (final Tensor<String> t = Tensors.create(bytes)) {
        final Session.Runner runner = model.instance()
            .session()
            .runner()
            .feed("input_example_tensor", t);
        final Map<String, JTensor> result = TensorFlowExtras.runAndExtract(runner, fetchOp);

        return outTensorExtractor.apply(result.get(fetchOp));
      }
    };

    return (model, vectors) -> {
      final List<CompletableFuture<Prediction<InputT, ValueT>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> predictFn.apply(model, vector.value()))
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());

      return CompletableFutures.allAsList(predictions);
    };
  }

}
