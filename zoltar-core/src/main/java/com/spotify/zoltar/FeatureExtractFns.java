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
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;

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
   * @param <ValueT> type of feature extraction result.
   */
  @FunctionalInterface
  interface ExtractFn<InputT, ValueT> {

    /**
     * Functional interface. Perform feature extraction.
     */
    List<ValueT> apply(InputT... inputs) throws Exception;
  }

  /**
   * Generic feature extraction function, takes a single raw input and should return extracted
   * features of user defined type.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <ValueT> type of feature extraction result.
   */
  @FunctionalInterface
  interface SingleExtractFn<InputT, ValueT> extends ExtractFn<InputT, ValueT> {

    /**
     * Functional interface. Perform feature extraction.
     */
    ValueT apply(InputT input) throws Exception;

    default List<ValueT> apply(final InputT... inputs) throws Exception {
      final Builder<ValueT> builder = ImmutableList.builder();

      for(final InputT input: inputs) {
        builder.add(apply(input));
      }

      return builder.build();
    }
  }

}
