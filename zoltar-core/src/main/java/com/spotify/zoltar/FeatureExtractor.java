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
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractFns.FeatranExtractFn;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
   * href="https://github.com/spotify/featran">Featran</a> and {@link FeatranExtractFn} whenever
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

  /**
   * Creates an Featran based feature extractor.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings Featran's settings.
   * @param fn {@link FeatranExtractFn} function, for example {@link JFeatureSpec#extractWithSettingsExample}
   * @param <InputT> type of the input to the {@link FeatureSpec}.
   * @param <ValueT> type of the output from {@link FeatranExtractFn}.
   */
  static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
      final FeatureSpec<InputT> featureSpec,
      final String settings,
      final FeatranExtractFn<InputT, ValueT> fn) {
    return create(JFeatureSpec.wrap(featureSpec), settings, fn);
  }

  /**
   * Creates an Featran based feature extractor.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings Featran's settings.
   * @param fn {@link FeatranExtractFn} function, for example {@link JFeatureSpec#extractWithSettingsExample}
   * @param <InputT> type of the input to the {@link FeatureSpec}.
   * @param <ValueT> type of the output from {@link FeatranExtractFn}.
   */
  static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
      final JFeatureSpec<InputT> featureSpec,
      final String settings,
      final FeatranExtractFn<InputT, ValueT> fn) {
    final JRecordExtractor<InputT, ValueT> extractor = fn.apply(featureSpec, settings);
    return inputs -> Arrays.stream(inputs)
        .map(i -> Vector.create(i, extractor.featureValue(i)))
        .collect(Collectors.toList());
  }

  /** Functional interface. Perform the feature extraction given the input. */
  List<Vector<InputT, ValueT>> extract(InputT... input) throws Exception;
}
