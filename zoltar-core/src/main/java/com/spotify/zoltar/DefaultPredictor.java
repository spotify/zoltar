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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;

/**
 * Allows E2E prediction given a recipe made of a {@link Model}, {@link FeatureExtractor} and a
 * {@link PredictFn}.
 *
 * @param <InputT> type of the feature extraction input.
 * @param <ValueT> type of the prediction output.
 */
@FunctionalInterface
interface DefaultPredictor<InputT, ValueT> extends Predictor<InputT, ValueT> {

  /**
   * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> DefaultPredictor<InputT, ValueT> create(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return (scheduler, timeout, inputs) -> {
      final CompletableFuture<List<Prediction<InputT, ValueT>>> future =
          modelLoader
              .get()
              .thenCompose(
                  model -> {
                    try {
                      return predictFn.apply(model, featureExtractor.extract(model, inputs));
                    } catch (final Exception e) {
                      throw new CompletionException(e);
                    }
                  })
              .toCompletableFuture();

      final ScheduledFuture<?> schedule =
          scheduler.schedule(
              () -> {
                future.completeExceptionally(new TimeoutException());
              },
              timeout.toMillis(),
              TimeUnit.MILLISECONDS);

      future.whenComplete((r, t) -> schedule.cancel(true));

      return future;
    };
  }
}
