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

import com.google.auto.value.AutoValue;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.PredictorBuilder;
import java.util.function.Function;

/**
 * Implementation of an instrumented {@link PredictorBuilder}.
 *
 * @param <ModelT>  underlying type of the {@link Model}.
 * @param <InputT>  type of the input to the {@link FeatureExtractor}.
 * @param <VectorT> type of the output from {@link FeatureExtractor}.
 * @param <ValueT>  type of the prediction result.
 */
@AutoValue
public abstract class InstrumentedPredictorBuilder<ModelT extends Model<?>, InputT, VectorT, ValueT>
    implements PredictorBuilder<ModelT, InputT, VectorT, ValueT> {

  public abstract PredictorBuilder<ModelT, InputT, VectorT, ValueT> predictorBuilder();

  public abstract PredictorMetrics metrics();

  /**
   * Creates a new instrumented {@link PredictorBuilder}.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> Function<PredictorBuilder<ModelT, InputT, VectorT, ValueT>, InstrumentedPredictorBuilder<ModelT, InputT, VectorT, ValueT>> create(
      final PredictorMetrics metrics) {
    return predictorBuilder ->
        new AutoValue_InstrumentedPredictorBuilder<>(predictorBuilder, metrics);
  }

  @Override
  public ModelLoader<ModelT> modelLoader() {
    return predictorBuilder().modelLoader();
  }

  @Override
  public FeatureExtractor<InputT, VectorT> featureExtractor() {
    final FeatureExtractorMetrics featureExtractorMetrics = metrics().featureExtractorMetrics();
    return predictorBuilder()
        .featureExtractor()
        .with(InstrumentedFeatureExtractor.create(featureExtractorMetrics));
  }

  @Override
  public AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn() {
    final PredictFnMetrics predictFnMetrics = metrics().predictFnMetrics();
    return predictorBuilder()
        .predictFn()
        .with(InstrumentedPredictFn.create(predictFnMetrics));
  }

  @Override
  public Predictor<InputT, ValueT> predictor(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return predictorBuilder().predictor(modelLoader, featureExtractor, predictFn);
  }
}
