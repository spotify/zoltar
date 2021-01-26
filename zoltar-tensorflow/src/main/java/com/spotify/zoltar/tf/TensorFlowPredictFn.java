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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.proto.example.Example;
import org.tensorflow.types.TString;

import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Vector;

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
   * @param outTensorExtractor Function to extract the output value from Tensor's
   * @param fetchOps operations to fetch.
   */
  static <InputT, ValueT> TensorFlowPredictFn<InputT, Example, ValueT> example(
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String... fetchOps) {
    return (model, vectors) ->
        CompletableFuture.supplyAsync(
            () -> {
              final byte[][] bytes =
                  vectors
                      .stream()
                      .map(Vector::value)
                      .map(Example::toByteArray)
                      .toArray(byte[][]::new);
              final NdArray<byte[]> examplesNdArray = NdArrays.vectorOfObjects(bytes);

              try (final Tensor<TString> t = TString.tensorOfBytes(examplesNdArray)) {
                final Session.Runner runner =
                    model.instance().session().runner().feed("input_example_tensor", t);
                final Map<String, Tensor<?>> result =
                    TensorFlowExtras.runAndExtract(runner, fetchOps);

                final Iterator<Vector<InputT, Example>> vectorIterator = vectors.iterator();
                final Iterator<ValueT> valueTIterator = outTensorExtractor.apply(result).iterator();
                final List<Prediction<InputT, ValueT>> predictions = new ArrayList<>();
                while (vectorIterator.hasNext() && valueTIterator.hasNext()) {
                  predictions.add(
                      Prediction.create(vectorIterator.next().input(), valueTIterator.next()));
                }
                return predictions;
              }
            });
  }

  /**
   * TensorFlow Example prediction function.
   *
   * @deprecated Use {@link #example(Function, String...)}
   * @param outTensorExtractor Function to extract the output value from Tensor's
   * @param fetchOps operations to fetch.
   */
  @Deprecated
  static <InputT, ValueT> TensorFlowPredictFn<InputT, List<Example>, ValueT> exampleBatch(
      final Function<Map<String, Tensor<?>>, ValueT> outTensorExtractor, final String... fetchOps) {
    final BiFunction<TensorFlowModel, List<Example>, ValueT> predictFn =
        (model, examples) -> {
          final byte[][] bytes = examples.stream().map(Example::toByteArray).toArray(byte[][]::new);
          final NdArray<byte[]> examplesNdArray = NdArrays.vectorOfObjects(bytes);

          try (final Tensor<TString> t = TString.tensorOfBytes(examplesNdArray)) {
            final Session.Runner runner =
                model.instance().session().runner().feed("input_example_tensor", t);
            final Map<String, Tensor<?>> result = TensorFlowExtras.runAndExtract(runner, fetchOps);

            return outTensorExtractor.apply(result);
          }
        };

    return (model, vectors) -> {
      final List<CompletableFuture<Prediction<InputT, ValueT>>> predictions =
          vectors
              .stream()
              .map(
                  vector ->
                      CompletableFuture.supplyAsync(() -> predictFn.apply(model, vector.value()))
                          .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());

      return CompletableFutures.allAsList(predictions);
    };
  }
}
