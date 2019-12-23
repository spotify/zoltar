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

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;

/** Predictors test helper methods. */
public final class PredictorsTest {

  private PredictorsTest() {}

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor}and a {@link
   * PredictFn}.
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
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
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
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
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
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
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
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn);
  }
}
