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
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import java.util.List;
import java.util.stream.Collectors;

public interface Model<UnderlyingT, SpecT> extends AutoCloseable {

  @FunctionalInterface
  interface PredictFn<ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> {

    List<Prediction<SpecT, ValueT>> apply(ModelT model, List<Vector<SpecT, VectorT>> vectors)
        throws Exception;
  }

  @FunctionalInterface
  interface Predictor<SpecT, ValueT> {

    static <ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> Predictor<SpecT, ValueT> create(
        ModelT model,
        FeatureExtractFn<SpecT, VectorT> featureExtractFn,
        PredictFn<ModelT, SpecT, VectorT, ValueT> predictFn) {
      return input -> {
        final List<Vector<SpecT, VectorT>> vectors = FeatureExtractor
            .create(model, featureExtractFn)
            .extract(input);

        return predictFn.apply(model, vectors);
      };
    }

    List<Prediction<SpecT, ValueT>> predict(List<SpecT> input) throws Exception;
  }

  @FunctionalInterface
  interface FeatureExtractFn<SpecT, ValueT> {

    List<ValueT> apply(JFeatureExtractor<SpecT> fn) throws Exception;
  }

  @FunctionalInterface
  interface FeatureExtractor<SpecT, ValueT> {

    static <SpecT, ValueT> FeatureExtractor<SpecT, ValueT> create(
        Model<?, SpecT> model,
        FeatureExtractFn<SpecT, ValueT> fn) {
      return inputs -> {
        final JFeatureExtractor<SpecT> extractor = model.featureSpec()
            .extractWithSettings(inputs, model.settings());

        return Streams.zip(inputs.stream(), fn.apply(extractor).stream(), Vector::create)
            .collect(Collectors.toList());
      };
    }

    List<Vector<SpecT, ValueT>> extract(List<SpecT> input) throws Exception;
  }

  @AutoValue
  abstract class Vector<SpecT, ValueT> {

    public abstract SpecT input();

    public abstract ValueT value();

    public static <SpecT, ValueT> Vector<SpecT, ValueT> create(SpecT input, ValueT value) {
      return new AutoValue_Model_Vector<>(input, value);
    }
  }

  @AutoValue
  abstract class Prediction<SpecT, ValueT> {

    public abstract SpecT input();

    public abstract ValueT value();

    public static <SpecT, ValueT> Prediction<SpecT, ValueT> create(SpecT input, ValueT value) {
      return new AutoValue_Model_Prediction<>(input, value);
    }
  }

  UnderlyingT instance();

  String settings();

  JFeatureSpec<SpecT> featureSpec();

}
