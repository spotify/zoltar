/*-
 * -\-\-
 * zoltar-core
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

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Feature extraction functions. Functions used to transform raw input into extracted features,
 * should be used together with {@link FeatureExtractor}.
 *
 * @see FeatureExtractor
 */
public interface FeatureExtractFns {

  /**
   * Generic feature extraction function, takes multiple raw input and should return extracted
   * features of user defined type.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <VectorT> type of feature extraction result.
   */
  @FunctionalInterface
  interface ExtractFn<InputT, VectorT> {

    static <InputT, VectorT> ExtractFn<InputT, VectorT> lift(final Function<InputT, VectorT> fn) {
      return inputs -> Arrays.stream(inputs).map(fn).collect(Collectors.toList());
    }

    /**
     * Functional interface. Perform feature extraction.
     */
    List<VectorT> apply(InputT... inputs) throws Exception;

    default <C extends ExtractFn<InputT, VectorT>> C with(
        final Function<ExtractFn<InputT, VectorT>, C> fn) {
      return fn.apply(this);
    }
  }

  /**
   * Generic feature extraction function, takes a batch of raw inputs and should return extracted
   * features of user defined type for each input.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <VectorT> type of feature extraction result.
   */
  @FunctionalInterface
  interface BatchExtractFn<InputT, VectorT> extends ExtractFn<List<InputT>, List<VectorT>> {

    static <InputT, VectorT> BatchExtractFn<InputT, VectorT> lift(
        final ExtractFn<InputT, VectorT> fn) {
      return inputs -> {
        final ImmutableList.Builder<List<VectorT>> output = ImmutableList.builder();
        for (final List<InputT> batch: inputs) {
          final InputT[] objects = (InputT[]) batch.toArray(new Object[batch.size()]);
          output.add(fn.apply(objects));
        }

        return output.build();
      };
    }

    static <InputT, VectorT> BatchExtractFn<InputT, VectorT> lift(
        final Function<InputT, VectorT> fn) {
      return lift(ExtractFn.lift(fn));
    }
  }
}
