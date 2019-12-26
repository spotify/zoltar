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

import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Predictor;

/**
 * Implementation of an instrumented {@link Predictor}.
 *
 * @param <ModelT> underlying type of the {@link Model}.
 * @param <InputT> type of the input to the {@link FeatureExtractor}.
 * @param <VectorT> type of the output from {@link FeatureExtractor}.
 * @param <ValueT> type of the prediction result.
 */
public final class InstrumentedPredictor<ModelT extends Model<?>, InputT, VectorT, ValueT>
    implements Predictor<ModelT, InputT, VectorT, ValueT> {

  private Predictor<ModelT, InputT, VectorT, ValueT> predictor;

  private InstrumentedPredictor(final Predictor<ModelT, InputT, VectorT, ValueT> predictor) {
    this.predictor = predictor;
  }

  public ModelLoader<ModelT> modelLoader() {
    return predictor.modelLoader();
  }

  public FeatureExtractor<ModelT, InputT, VectorT> featureExtractor() {
    return predictor.featureExtractor();
  }

  public AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn() {
    return predictor.predictFn();
  }

  /** Creates a new instrumented {@link Predictor}. */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      InstrumentedPredictor<ModelT, InputT, VectorT, ValueT> create(
          final Predictor<ModelT, InputT, VectorT, ValueT> predictor,
          final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    final FeatureExtractorMetrics<InputT, VectorT> featureExtractorMetrics =
        metrics.featureExtractorMetrics();
    final InstrumentedFeatureExtractor<ModelT, InputT, VectorT> featureExtractor =
        predictor
            .featureExtractor()
            .with(InstrumentedFeatureExtractor.create(featureExtractorMetrics));
    final PredictFnMetrics<InputT, ValueT> predictFnMetrics = metrics.predictFnMetrics();
    final InstrumentedPredictFn<ModelT, InputT, VectorT, ValueT> predictFn =
        predictor.predictFn().with(InstrumentedPredictFn.create(predictFnMetrics));

    final Predictor<ModelT, InputT, VectorT, ValueT> instrumented =
        predictor.toBuilder().featureExtractor(featureExtractor).predictFn(predictFn).build();

    return new InstrumentedPredictor<>(instrumented);
  }
}
