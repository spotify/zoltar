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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;

/**
 * Entry point for prediction, it allows to perform E2E prediction given a recipe made of a {@link
 * Model}, {@link FeatureExtractor} and a {@link PredictFn}. In most cases you should use the static
 * factory methods.
 *
 * @param <InputT> type of the feature extraction input.
 * @param <ValueT> type of the prediction output.
 */
public interface Predictor<ModelT extends Model<?>, InputT, VectorT, ValueT> {

  ModelLoader<ModelT> modelLoader();

  FeatureExtractor<ModelT, InputT, VectorT> featureExtractor();

  AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn();

  default Builder<ModelT, InputT, VectorT, ValueT> toBuilder() {
    return new Builder<>(modelLoader(), featureExtractor(), predictFn());
  }

  default <C extends Predictor<ModelT, InputT, VectorT, ValueT>> C with(
      final Function<Predictor<ModelT, InputT, VectorT, ValueT>, C> fn) {
    return fn.apply(this);
  }

  /**
   * Perform prediction.
   *
   * @param executionContext prediction execution context.
   * @param inputs a list of inputs to perform feature extraction and prediction on.
   * @param timeout implementation specific timeout.
   */
  @SuppressWarnings("checkstyle:LineLength")
  CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      ScheduledExecutorService scheduler, Duration timeout, List<InputT> input);

  /** Perform prediction with a default scheduler. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      final Duration timeout, final List<InputT> input) {
    return predict(timeoutScheduler().scheduler(), timeout, input);
  }

  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      final Duration timeout, final InputT input) {
    return predict(timeoutScheduler().scheduler(), timeout, Collections.singletonList(input));
  }

  /** Perform prediction with a default scheduler, and practically infinite timeout. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final List<InputT> input) {
    return predict(timeoutScheduler().scheduler(), Duration.ofDays(Integer.MAX_VALUE), input);
  }

  /** Perform prediction with a default scheduler, and practically infinite timeout. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final InputT input) {
    return predict(
        timeoutScheduler().scheduler(),
        Duration.ofDays(Integer.MAX_VALUE),
        Collections.singletonList(input));
  }
}
