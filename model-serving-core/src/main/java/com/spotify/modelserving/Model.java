/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Streams;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import java.util.List;
import java.util.stream.Collectors;

public interface Model<M, T> extends AutoCloseable {

  @FunctionalInterface
  interface PredictFn<M extends Model<?, T>, T, V, U> {

    List<Prediction<T, U>> apply(M model, List<Vector<T, V>> vectors)
        throws Exception;
  }

  @FunctionalInterface
  interface Predictor<I, U> {

    static <X extends Model<?, T>, T, U, V> Predictor<T, U> create(
        X model,
        FeatureExtractFn<T, V> featureExtractFn,
        PredictFn<X, T, V, U> predictFn) {
      return input -> {
        final List<Vector<T, V>> vectors = FeatureExtractor
            .create(model, featureExtractFn)
            .extract(input);

        return predictFn.apply(model, vectors);
      };
    }

    List<Prediction<I, U>> predict(List<I> input) throws Exception;
  }

  @FunctionalInterface
  interface FeatureExtractFn<T, V> {

    List<V> apply(JFeatureExtractor<T> fn) throws Exception;
  }

  @FunctionalInterface
  interface FeatureExtractor<T, U> {

    static <T, U> FeatureExtractor<T, U> create(Model<?, T> model, FeatureExtractFn<T, U> fn) {
      return inputs -> {
        final JFeatureExtractor<T> extractor = JFeatureSpec.wrap(model.featureSpec())
            .extractWithSettings(inputs, model.settings());

        return Streams.zip(inputs.stream(), fn.apply(extractor).stream(), Vector::create)
            .collect(Collectors.toList());
      };
    }

    List<Vector<T, U>> extract(List<T> input) throws Exception;
  }

  @AutoValue
  abstract class Vector<T, V> {

    public abstract T input();

    public abstract V value();

    public static <T, V> Vector<T, V> create(T input, V value) {
      return new AutoValue_Model_Vector<>(input, value);
    }
  }

  @AutoValue
  abstract class Prediction<T, V> {

    public abstract T input();

    public abstract V value();

    public static <T, V> Prediction<T, V> create(T input, V value) {
      return new AutoValue_Model_Prediction<>(input, value);
    }
  }

  M instance();

  String settings();

  FeatureSpec<T> featureSpec();

}
