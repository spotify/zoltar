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

import com.google.common.collect.Lists;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Functional interface for feature extraction. Should be used together with {@link Predictor}. In
 * most cases you should use the static factory methods.
 *
 * @param <InputT> type of the input to feature extraction.
 * @param <ValueT> type of feature extraction result.
 */
@FunctionalInterface
public interface FeatureExtractor<InputT, ValueT> {

  /**
   * Creates an extractor given a generic {@link ExtractFn}, consider using <a
   * href="https://github.com/spotify/featran">Featran</a> and FeatranExtractFns whenever
   * possible.
   *
   * @param fn {@link ExtractFn} extraction function
   * @param <InputT> type of the input to feature extraction.
   * @param <ValueT> type of feature extraction result.
   */
  static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
      final ExtractFn<InputT, ValueT> fn) {
    return inputs -> {
      final List<Vector<InputT, ValueT>> result = Lists.newArrayList();
      final Iterator<InputT> i1 = Arrays.asList(inputs).iterator();
      final Iterator<ValueT> i2 = fn.apply(inputs).iterator();
      while (i1.hasNext() && i2.hasNext()) {
        result.add(Vector.create(i1.next(), i2.next()));
      }
      return result;
    };
  }

  /** Functional interface. Perform the feature extraction given the input. */
  List<Vector<InputT, ValueT>> extract(InputT... input) throws Exception;
}
