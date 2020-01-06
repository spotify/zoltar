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
package com.spotify.zoltar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

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
      return inputs -> {
        final List<VectorT> result = new ArrayList<>();
        for (InputT inputT : inputs) {
          result.add(fn.apply(inputT));
        }
        return result;
      };
    }

    static <InputT> ExtractFn<InputT, InputT> identity() {
      return Arrays::asList;
    }

    /** Functional interface. Perform feature extraction. */
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
   * @deprecated Use {@link ExtractFn} instead
   * @param <InputT> type of the input to feature extraction.
   * @param <VectorT> type of feature extraction result.
   */
  @FunctionalInterface
  @Deprecated
  interface BatchExtractFn<InputT, VectorT> extends ExtractFn<List<InputT>, List<VectorT>> {

    @SuppressWarnings("unchecked")
    static <InputT, VectorT> BatchExtractFn<InputT, VectorT> lift(
        final ExtractFn<InputT, VectorT> fn) {
      return inputs -> {
        final ImmutableList.Builder<List<VectorT>> output = ImmutableList.builder();
        for (final List<InputT> batch : inputs) {
          final InputT[] objects = batch.toArray((InputT[]) new Object[0]);
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
