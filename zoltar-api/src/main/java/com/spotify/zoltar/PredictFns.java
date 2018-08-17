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

import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.tf.JTensor;
import com.spotify.zoltar.tf.TensorFlowExtras;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;
import java.util.Arrays;
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
 * Something new or something old.
 */
public final class PredictFns {

  private PredictFns() {

  }

  /**
   * Something new or something old.
   */
  static final class TensorFlow {

    /**
     * Something new or something old.
     *
     * @param outTensorExtractor asd.
     * @param fetchOp            .asd
     */
    public static <InputT, ValueT> TensorFlowPredictFn<InputT, Example, ValueT> examplePredictFn(
        final Function<JTensor, ValueT> outTensorExtractor,
        final String fetchOp) {

      return (model, vectors) -> {
        final List<Vector<List<InputT>, List<Example>>> bulk = vectors.stream()
            .map(vector -> {
              final List<InputT> inputs = Collections.singletonList(vector.input());
              final List<Example> examples = Collections.singletonList(vector.value());
              return Vector.create(inputs, examples);
            }).collect(Collectors.toList());

        return TensorFlow
            .<InputT, ValueT>examplePredictFn(outTensorExtractor, new String[]{fetchOp})
            .apply(model, bulk)
            .thenApply(predictions -> {
              return predictions.stream()
                  .map(p -> Prediction.create(p.input().get(0), p.value().get(0)))
                  .collect(Collectors.toList());
            });
      };
    }

    /**
     * Something new or something old.
     *
     * @param outTensorExtractor asd.
     * @param fetchOps           asd.
     */
    @SuppressWarnings("checkstyle:LineLength")
    public static <InputT, ValueT> TensorFlowPredictFn<List<InputT>, List<Example>, List<ValueT>> examplePredictFn(
        final Function<JTensor, ValueT> outTensorExtractor,
        final String... fetchOps) {

      final BiFunction<TensorFlowModel, List<Example>, List<ValueT>> predictFn = (model, examples) -> {
        final byte[][] bytes = examples.stream().map(Example::toByteArray).toArray(byte[][]::new);

        try (final Tensor<String> t = Tensors.create(bytes)) {
          final Session.Runner runner = model.instance().session().runner()
              .feed("input_example_tensor", t);
          final Map<String, JTensor> result = TensorFlowExtras.runAndExtract(runner, fetchOps);

          return Arrays.stream(fetchOps)
              .map(result::get)
              .map(outTensorExtractor)
              .collect(Collectors.toList());
        }
      };

      return (model, vectors) -> {
        final List<CompletableFuture<Prediction<List<InputT>, List<ValueT>>>> predictions =
            vectors.stream()
                .map(vector -> CompletableFuture
                    .supplyAsync(() -> predictFn.apply(model, vector.value()))
                    .thenApply(v -> Prediction.create(vector.input(), v)))
                .collect(Collectors.toList());

        return CompletableFutures.allAsList(predictions);
      };
    }
  }

}
