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
package com.spotify.zoltar;

import com.google.auto.value.AutoValue;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;

/**
 * Entry point for prediction. Default implementation of a PredictorBuilder that holds the necessary
 * info to build a {@link Predictor}.
 *
 * @param <ModelT> underlying type of the {@link Model}.
 * @param <InputT> type of the input to the {@link FeatureExtractor}.
 * @param <VectorT> type of the output from {@link FeatureExtractor}.
 * @param <ValueT> type of the prediction result.
 */
@AutoValue
abstract class DefaultPredictorBuilder<ModelT extends Model<?>, InputT, VectorT, ValueT>
    implements PredictorBuilder<ModelT, InputT, VectorT, ValueT> {

  /**
   * Returns a context given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      DefaultPredictorBuilder<ModelT, InputT, VectorT, ValueT> create(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, FeatureExtractor.create(extractFn), AsyncPredictFn.lift(predictFn));
  }

  /**
   * Returns a context given a {@link Model}, {@link FeatureExtractor} and a {@link AsyncPredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      DefaultPredictorBuilder<ModelT, InputT, VectorT, ValueT> create(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, FeatureExtractor.create(extractFn), predictFn);
  }

  /**
   * Returns a context given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      DefaultPredictorBuilder<ModelT, InputT, VectorT, ValueT> create(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, featureExtractor, AsyncPredictFn.lift(predictFn));
  }

  /**
   * Returns a context given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      DefaultPredictorBuilder<ModelT, InputT, VectorT, ValueT> create(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return new AutoValue_DefaultPredictorBuilder<>(
        modelLoader,
        featureExtractor,
        predictFn,
        DefaultPredictor.create(modelLoader, featureExtractor, predictFn));
  }

  public abstract ModelLoader<ModelT> modelLoader();

  public abstract FeatureExtractor<ModelT, InputT, VectorT> featureExtractor();

  public abstract AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn();

  public abstract Predictor<InputT, ValueT> predictor();

  @Override
  public DefaultPredictorBuilder<ModelT, InputT, VectorT, ValueT> with(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, featureExtractor, predictFn);
  }
}
