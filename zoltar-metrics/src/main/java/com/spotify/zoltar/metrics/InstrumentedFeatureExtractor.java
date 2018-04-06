/*-
 * -\-\-
 * zoltar-metrics
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

package com.spotify.zoltar.metrics;

import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Vector;
import java.util.List;
import java.util.function.Function;


/**
 * Instrumented Functional interface for feature extraction {@link FeatureExtractor}.
 *
 * @param <InputT> type of the input to feature extraction.
 * @param <ValueT> type of feature extraction result.
 */
@FunctionalInterface
interface InstrumentedFeatureExtractor<InputT, ValueT>
    extends FeatureExtractor<InputT, ValueT> {

  /**
   * Creates a new instrumented {@link FeatureExtractor}.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <InputT, ValueT> Function<FeatureExtractor<InputT, ValueT>, InstrumentedFeatureExtractor<InputT, ValueT>> create(
      final FeatureExtractorMetrics metrics) {
    return extractFn -> inputs -> {
      final VectorMetrics vectorMetrics = metrics.get();
      final List<Vector<InputT, ValueT>> result = extractFn.extract(inputs);

      vectorMetrics.extraction(result);

      return result;
    };
  }

}
