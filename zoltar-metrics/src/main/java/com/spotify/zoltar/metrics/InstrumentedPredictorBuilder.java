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
package com.spotify.zoltar.metrics;

import java.util.function.Function;

import com.google.auto.value.AutoValue;

import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.PredictorBuilder;

/**
 * Implementation of an instrumented {@link PredictorBuilder}.
 *
 * @param <ModelT> underlying type of the {@link Model}.
 * @param <InputT> type of the input to the {@link FeatureExtractor}.
 * @param <VectorT> type of the output from {@link FeatureExtractor}.
 * @param <ValueT> type of the prediction result.
 */
@AutoValue
public abstract class InstrumentedPredictorBuilder<ModelT extends Model<?>, InputT, VectorT, ValueT>
    implements PredictorBuilder<ModelT, InputT, VectorT, ValueT> {

  public abstract PredictorBuilder<ModelT, InputT, VectorT, ValueT> predictorBuilder();

  public abstract PredictorMetrics<InputT, VectorT, ValueT> metrics();

  /** Creates a new instrumented {@link PredictorBuilder}. */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      Function<
              PredictorBuilder<ModelT, InputT, VectorT, ValueT>,
              InstrumentedPredictorBuilder<ModelT, InputT, VectorT, ValueT>>
          create(final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return predictorBuilder -> {
      final FeatureExtractorMetrics<InputT, VectorT> featureExtractorMetrics =
          metrics.featureExtractorMetrics();
      final InstrumentedFeatureExtractor<ModelT, InputT, VectorT> featureExtractor =
          predictorBuilder
              .featureExtractor()
              .with(InstrumentedFeatureExtractor.create(featureExtractorMetrics));
      final PredictFnMetrics<InputT, ValueT> predictFnMetrics = metrics.predictFnMetrics();
      final InstrumentedPredictFn<ModelT, InputT, VectorT, ValueT> predictFn =
          predictorBuilder.predictFn().with(InstrumentedPredictFn.create(predictFnMetrics));

      final ModelLoader<ModelT> modelLoader = predictorBuilder.modelLoader();

      final PredictorBuilder<ModelT, InputT, VectorT, ValueT> pb =
          predictorBuilder.with(modelLoader, featureExtractor, predictFn);

      return new AutoValue_InstrumentedPredictorBuilder<>(pb, metrics);
    };
  }

  @Override
  public ModelLoader<ModelT> modelLoader() {
    return predictorBuilder().modelLoader();
  }

  @Override
  public FeatureExtractor<ModelT, InputT, VectorT> featureExtractor() {
    return predictorBuilder().featureExtractor();
  }

  @Override
  public AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn() {
    return predictorBuilder().predictFn();
  }

  @Override
  public Predictor<InputT, ValueT> predictor() {
    return predictorBuilder().predictor();
  }

  @Override
  public InstrumentedPredictorBuilder<ModelT, InputT, VectorT, ValueT> with(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    final PredictorBuilder<ModelT, InputT, VectorT, ValueT> pb =
        predictorBuilder().with(modelLoader, featureExtractor, predictFn);

    return InstrumentedPredictorBuilder.<ModelT, InputT, VectorT, ValueT>create(metrics())
        .apply(pb);
  }
}
