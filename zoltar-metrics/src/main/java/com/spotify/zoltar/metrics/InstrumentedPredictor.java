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

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Predictor;

public interface InstrumentedPredictor<InputT, ValueT> extends Predictor<InputT, ValueT> {

  /**
   * Returns a instrumented predictor given a {@link Model}, {@link FeatureExtractor} and a
   * {@link PredictFn}.
   *
   * @param model     model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param metrics   metrics implementation.
   * @param <ModelT>  underlying type of the {@link Model}.
   * @param <InputT>  type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT>  type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> InstrumentedPredictor<InputT, ValueT> create(
      final ModelT model,
      final ExtractFn<InputT, VectorT> extractFn,
      final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
      final PredictorMetrics metrics) {
    return create(model,
                  FeatureExtractor.create(extractFn),
                  AsyncPredictFn.lift(predictFn),
                  metrics);
  }

  /**
   * Returns a instrumented predictor given a {@link Model}, {@link FeatureExtractor} and a
   * {@link PredictFn}.
   *
   * @param model            model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn        a prediction function to perform prediction with {@link PredictFn}.
   * @param metrics   metrics implementation.
   * @param <ModelT>         underlying type of the {@link Model}.
   * @param <InputT>         type of the input to the {@link FeatureExtractor}.
   * @param <VectorT>        type of the output from {@link FeatureExtractor}.
   * @param <ValueT>         type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> InstrumentedPredictor<InputT, ValueT> create(
      final ModelT model,
      final FeatureExtractor<InputT, VectorT> featureExtractor,
      final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
      final PredictorMetrics metrics) {
    return create(model, featureExtractor, AsyncPredictFn.lift(predictFn), metrics);
  }

  /**
   * Returns a instrumented predictor given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
   *
   * @param model     model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param metrics   metrics implementation.
   * @param <ModelT>  underlying type of the {@link Model}.
   * @param <InputT>  type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT>  type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> InstrumentedPredictor<InputT, ValueT> create(
      final ModelT model,
      final ExtractFn<InputT, VectorT> extractFn,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
      final PredictorMetrics metrics) {
    return create(model, FeatureExtractor.create(extractFn), predictFn, metrics);
  }

  /**
   * Returns a instrumented predictor given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
   *
   * @param model            model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn        a prediction function to perform prediction with {@link
   *                         AsyncPredictFn}.
   * @param metrics          metrics implementation.
   * @param <ModelT>         underlying type of the {@link Model}.
   * @param <InputT>         type of the input to the {@link FeatureExtractor}.
   * @param <VectorT>        type of the output from {@link FeatureExtractor}.
   * @param <ValueT>         type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> InstrumentedPredictor<InputT, ValueT> create(
      final ModelT model,
      final FeatureExtractor<InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
      final PredictorMetrics metrics) {
    final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> timedPredictFn = metrics.timed(predictFn);
    final FeatureExtractor<InputT, VectorT> timedFeatureExtractor = metrics.timed(featureExtractor);

    return Predictor.create(model, timedFeatureExtractor, timedPredictFn)::predict;
  }

}
