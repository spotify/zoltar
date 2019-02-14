/*
 * Copyright (C) 2016 - 2018 Spotify AB
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
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      final ExecutionContext<InputT, ValueT> executionContext,
      final Duration timeout,
      final InputT... inputs) {
    return executionContext.predict(timeout, inputs);
  }

  /** Perform prediction with a default scheduler. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(
      final Duration timeout, final InputT... input) {
    return predict(ExecutionContext.create(this), timeout, input);
  }

  /** Perform prediction with a default scheduler, and practically infinite timeout. */
  default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final InputT... input) {
    return predict(ExecutionContext.create(this), Duration.ofDays(Integer.MAX_VALUE), input);
  }

  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      Builder<ModelT, InputT, VectorT, ValueT> builder() {
    return new Builder<>();
  }

  /**
   * Returns a Predictor given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
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
  static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      Builder<ModelT, InputT, VectorT, ValueT> builder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {

    return new Builder<>(modelLoader, featureExtractor, predictFn);
  }

  /** Predictor Builder instance. */
  class Builder<ModelT extends Model<?>, InputT, VectorT, ValueT> {

    private ModelLoader<ModelT> modelLoader;
    private FeatureExtractor<ModelT, InputT, VectorT> featureExtractor;
    private AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn;

    Builder() {}

    Builder(
        final ModelLoader<ModelT> modelLoader,
        final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
        final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {

      this.modelLoader = modelLoader;
      this.featureExtractor = featureExtractor;
      this.predictFn = predictFn;
    }

    public Builder<ModelT, InputT, VectorT, ValueT> modelLoader(
        final ModelLoader<ModelT> modelLoader) {
      this.modelLoader = modelLoader;
      return this;
    }

    public Builder<ModelT, InputT, VectorT, ValueT> featureExtractFn(
        final ExtractFn<InputT, VectorT> extractFn) {
      return featureExtractor(FeatureExtractor.create(extractFn));
    }

    public Builder<ModelT, InputT, VectorT, ValueT> featureExtractor(
        final FeatureExtractor<ModelT, InputT, VectorT> fe) {
      this.featureExtractor = fe;
      return this;
    }

    public Builder<ModelT, InputT, VectorT, ValueT> predictFn(
        final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
      return predictFn(AsyncPredictFn.lift(predictFn));
    }

    public Builder<ModelT, InputT, VectorT, ValueT> predictFn(
        final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
      this.predictFn = predictFn;
      return this;
    }

    public Predictor<ModelT, InputT, VectorT, ValueT> build() {
      return new Predictor<ModelT, InputT, VectorT, ValueT>() {
        @Override
        public ModelLoader<ModelT> modelLoader() {
          return modelLoader;
        }

        @Override
        public FeatureExtractor<ModelT, InputT, VectorT> featureExtractor() {
          return featureExtractor;
        }

        @Override
        public AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn() {
          return predictFn;
        }
      };
    }
  }

  /**
   * Prediction execution context.
   *
   * @param <InputT> type of the feature extraction input.
   * @param <ValueT> type of the prediction output.
   */
  @FunctionalInterface
  interface ExecutionContext<InputT, ValueT> {

    ScheduledExecutorService DEFAULT_SCHEDULER =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @SuppressWarnings("checkstyle:LineLength")
    static <ModelT extends Model<?>, InputT, VectorT, ValueT>
        ExecutionContext<InputT, ValueT> create(
            final Predictor<ModelT, InputT, VectorT, ValueT> predictor) {
      return create(predictor, defaultTimeoutScheduler());
    }

    @SuppressWarnings("checkstyle:LineLength")
    static <ModelT extends Model<?>, InputT, VectorT, ValueT>
        ExecutionContext<InputT, ValueT> create(
            final Predictor<ModelT, InputT, VectorT, ValueT> predictor,
            final ScheduledExecutorService scheduler) {
      return (timeout, inputs) -> {
        final CompletableFuture<List<Prediction<InputT, ValueT>>> future =
            predictor
                .modelLoader()
                .get()
                .thenCompose(
                    model -> {
                      try {
                        return predictor
                            .predictFn()
                            .apply(model, predictor.featureExtractor().extract(model, inputs));
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

    /** timeout scheduler for predict functions. */
    static ScheduledExecutorService defaultTimeoutScheduler() {
      return DEFAULT_SCHEDULER;
    }

    /**
     * Perform prediction.
     *
     * @param inputs a list of inputs to perform feature extraction and prediction on.
     * @param timeout implementation specific timeout.
     */
    CompletionStage<List<Prediction<InputT, ValueT>>> predict(Duration timeout, InputT... inputs);

    /** Perform prediction with a default scheduler, and practically infinite timeout. */
    default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final InputT... input) {
      return predict(Duration.ofDays(Integer.MAX_VALUE), input);
    }
  }
}
