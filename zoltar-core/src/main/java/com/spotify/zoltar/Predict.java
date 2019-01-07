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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Prediction functional interface. Allows to define prediction function via lambda.
 *
 * @param <ModelT>  underlying type of the {@link Model}.
 * @param <InputT>  type of the feature extraction input.
 * @param <VectorT> type of the feature extraction output.
 * @param <ValueT>  type of the prediction output.
 */
@FunctionalInterface
interface Predict<ModelT extends Model<?>, InputT, VectorT, ValueT> {

  /**
   * ConsPredict is a constant {@link Predict}.
   *
   * @param <ModelT>  underlying type of the {@link Model}.
   * @param <InputT>  type of the feature extraction input.
   * @param <VectorT> type of the feature extraction output.
   * @param <ValueT>  type of the prediction output.
   */
  @FunctionalInterface
  interface ConsPredict<ModelT extends Model<?>, InputT, VectorT, ValueT>
      extends Predict<ModelT, InputT, VectorT, ValueT> {

    /**
     * Creates a {@link Predict} with precomputed predictions.
     */
    static <M extends Model<?>, I, V, O> ConsPredict<M, I, V, O> cons(
        final List<Prediction<I, O>> predictions) {
      final CompletableFuture<List<Prediction<I, O>>> p =
          CompletableFuture.completedFuture(predictions);
      return (m, v) -> p;
    }
  }

  /**
   * ConsPredict is a constant {@link Predict}.
   *
   * @param <ModelT>  underlying type of the {@link Model}.
   * @param <InputT>  type of the feature extraction input.
   * @param <VectorT> type of the feature extraction output.
   * @param <ValueT>  type of the prediction output.
   */
  @FunctionalInterface
  interface TimeoutPredict<ModelT extends Model<?>, InputT, VectorT, ValueT>
      extends Predict<ModelT, InputT, VectorT, ValueT> {

    /**
     * Creates a {@link Predict} that will timeout if time exceeds {@link Duration}.
     */
    static <M extends Model<?>, I, V, O> TimeoutPredict<M, I, V, O> timeout(
        final Predict<M, I, V, O> predict,
        final Duration duration,
        final Executor executor) {
      return Predict.<M, I, V, O>predict((m, v) -> {
        try {
          return predict
              .apply(m, v)
              .toCompletableFuture()
              .get(duration.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, executor)::apply;
    }
  }

  /**
   * Synchronous prediction functional interface. Allows to define prediction function via lambda.
   *
   * @param <InputT> type of the feature extraction input.
   * @param <VectorT> type of the feature extraction output.
   * @param <ValueT> type of the prediction output.
   */
  @FunctionalInterface
  interface PredictFn<ModelT extends Model<?>, InputT, VectorT, ValueT> {

    /**
     * The functional interface. Your function/lambda takes model and features after extractions as
     * input, should perform a prediction and return the predictions.
     *
     * @param model model to perform prediction on.
     * @param vectors extracted features.
     * @return predictions ({@link Prediction}).
     */
    List<Prediction<InputT, ValueT>> apply(ModelT model, List<Vector<InputT, VectorT>> vectors);
  }

  /**
   * Creates a {@link Predict} with precomputed predictions.
   */
  static <M extends Model<?>, I, V, O> Predict<M, I, V, O> predicted(
      final List<Prediction<I, O>> predictions) {
    return ConsPredict.cons(predictions);
  }

  /**
   * Creates a {@link Predict} that will execute the supplied Function asynchronously.
   */
  static <M extends Model<?>, I, V, O> Predict<M, I, V, O> predict(
      final PredictFn<M, I, V, O> fn,
      final Executor executor) {
    return (model, vectors) -> CompletableFuture.supplyAsync(() -> {
      return fn.apply(model, vectors);
    }, executor);
  }

  /**
   * Creates a {@link Predict} that will timeout if time exceeds {@link Duration}.
   */
  static <M extends Model<?>, I, V, O> Predict<M, I, V, O> predict(
      final Predict<M, I, V, O> predict,
      final Duration duration,
      final Executor executor) {
    return TimeoutPredict.timeout(predict, duration, executor);
  }

  /**
   * The functional interface. Your function/lambda takes model and features after extractions as
   * input, should perform a prediction and return the predictions.
   *
   * @param model   model to perform prediction on.
   * @param vectors extracted features.
   * @return predictions ({@link Prediction}).
   */
  CompletionStage<List<Prediction<InputT, ValueT>>> apply(
      ModelT model,
      List<Vector<InputT, VectorT>> vectors);

  default <C extends Predict<ModelT, InputT, VectorT, ValueT>> C with(
      final Function<Predict<ModelT, InputT, VectorT, ValueT>, C> fn) {
    return fn.apply(this);
  }
}
