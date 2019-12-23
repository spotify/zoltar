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
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;

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
@FunctionalInterface
public interface Predictor<InputT, ValueT> {

  /** timeout scheduler for predict functions. */
  default PredictorTimeoutScheduler timeoutScheduler() {
    return DefaultPredictorTimeoutScheduler.create();
  }

  /**
   * Functional interface. You should perform E2E feature extraction and prediction. See {@link
   * DefaultPredictorBuilder#create(ModelLoader, FeatureExtractor, AsyncPredictFn)} for an example
   * of usage.
   *
   * @param input a list of inputs to perform feature extraction and prediction on.
   * @param timeout implementation specific timeout.
   * @param scheduler implementation specific scheduler.
   */
  @SuppressWarnings("checkstyle:LineLength")
  CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      ScheduledExecutorService scheduler, Duration timeout, InputT... input);

  /** Perform prediction with a default scheduler. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      final Duration timeout, final InputT... input) {
    return predict(timeoutScheduler().scheduler(), timeout, input);
  }

  /** Perform prediction with a default scheduler, and practically infinite timeout. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final InputT... input) {
    return predict(timeoutScheduler().scheduler(), Duration.ofDays(Integer.MAX_VALUE), input);
  }
}
