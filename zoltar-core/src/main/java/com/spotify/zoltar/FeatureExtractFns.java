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

import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import java.util.List;

/**
 * Feature extraction functions. Functions used to transform raw input into extracted features,
 * should be used together with {@link FeatureExtractor}.
 *
 * @see FeatureExtractor
 */
public interface FeatureExtractFns {

  /**
   * Generic feature extraction function, takes raw input and should return extracted features of
   * user defined type.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <ValueT> type of feature extraction result.
   */
  @FunctionalInterface
  interface ExtractFn<InputT, ValueT> {

    /**
     * Functional interface. Perform feature extraction.
     */
    List<ValueT> apply(List<InputT> inputs) throws Exception;
  }

  /**
   * <a href="https://github.com/spotify/featran">Featran</a> specific feature extraction function.
   *
   * @param <InputT> type of the input to the {@link JFeatureSpec}.
   * @param <ValueT> type of the output from {@link JRecordExtractor}.
   */
  @FunctionalInterface
  interface FeatranExtractFn<InputT, ValueT> {

    /**
     * Functional interface. Perform feature extraction given Featran's feature specification and
     * settings.
     *
     * @param spec Featran's feature spec.
     * @param settings Featran's settings.
     */
    JRecordExtractor<InputT, ValueT> apply(JFeatureSpec<InputT> spec, String settings);
  }
}
