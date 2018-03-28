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

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Entry point for prediction, it allows to perform E2E prediction given a recipe made of a {@link
 * Model}, {@link FeatureExtractor} and a {@link PredictFn}. In most cases you should use the static
 * factory methods.
 *
 * @param <InputT> type of the feature extraction input.
 * @param <ValueT> type of the prediction output.
 */
@FunctionalInterface
public interface Predictor<InputT, ValueT> {

  /** PreLoader scheduler for predict functions. */
  ScheduledExecutorService SCHEDULER =
      Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

  /**
   * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
      final ModelLoader<ModelT> modelLoader,
      final ExtractFn<InputT, VectorT> extractFn,
      final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, FeatureExtractor.create(extractFn), AsyncPredictFn.lift(predictFn));
  }

  /**
   * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<InputT, VectorT> featureExtractor,
      final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, featureExtractor, AsyncPredictFn.lift(predictFn));
  }

  /**
   * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a {@link
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
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
      final ModelLoader<ModelT> modelLoader,
      final ExtractFn<InputT, VectorT> extractFn,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return create(modelLoader, FeatureExtractor.create(extractFn), predictFn);
  }

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
  static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
      final ModelLoader<ModelT> modelLoader,
      final FeatureExtractor<InputT, VectorT> featureExtractor,
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return (scheduler, timeout, inputs) -> {
      final CompletableFuture<List<Prediction<InputT, ValueT>>> future = modelLoader.get()
          .thenCompose(model -> {
            try {
              return predictFn.apply(model, featureExtractor.extract(inputs));
            } catch (final Exception e) {
              throw new CompletionException(e);
            }
          })
          .toCompletableFuture();

      final ScheduledFuture<?> schedule = scheduler.schedule(() -> {
        future.completeExceptionally(new TimeoutException());
      }, timeout.toMillis(), TimeUnit.MILLISECONDS);

      future.whenComplete((r, t) -> schedule.cancel(true));

      return future;
    };
  }

  /**
   * Functional interface. You should perform E2E feature extraction and prediction. See {@link
   * Predictor#create(ModelLoader, FeatureExtractor, AsyncPredictFn)} for an example of usage.
   *
   * @param input a list of inputs to perform feature extraction and prediction on.
   * @param timeout implementation specific timeout, see {@link Predictor#create(ModelLoader,
   * FeatureExtractor, AsyncPredictFn)} for an example of usage.
   * @param scheduler implementation specific scheduler, see {@link Predictor#create(ModelLoader,
   * FeatureExtractor, AsyncPredictFn)} for an example of usage.
   */
  CompletionStage<List<Prediction<InputT, ValueT>>> predict(ScheduledExecutorService scheduler,
                                                            Duration timeout,
                                                            InputT... input);

  /** Perform prediction with a default scheduler. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final Duration timeout,
                                                                    final InputT... input) {
    return predict(SCHEDULER, timeout, input);
  }

  /** Perform prediction with a default scheduler, and practically infinite timeout. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final InputT... input) {
    return predict(SCHEDULER, Duration.ofDays(Integer.MAX_VALUE), input);
  }

}
