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
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Prediction function interfaces. Describe how to perform predictions on a model given input
 * vectors. See prebaked implementations for prebaked models.
 *
 * @see Predictor
 */
public interface PredictFns {

  /**
   * Asynchronous prediction functional interface. Allows to define prediction function via lambda,
   * prediction is happens asynchronously.
   *
   * @param <InputT> type of the feature extraction input.
   * @param <VectorT> type of the feature extraction output.
   * @param <ValueT> type of the prediction output.
   */
  @FunctionalInterface
  interface AsyncPredictFn<ModelT extends Model<?>, InputT, VectorT, ValueT> {

    /**
     * Lifts prediction function to asynchronous prediction function.
     *
     * @param <ModelT> type of the {@link Model}
     * @param <InputT> type of the feature extraction input.
     * @param <VectorT> type of the feature extraction output.
     * @param <ValueT> type of the prediction output.
     */
    @SuppressWarnings("checkstyle:LineLength")
    static <ModelT extends Model<?>, InputT, VectorT, ValueT>
        AsyncPredictFn<ModelT, InputT, VectorT, ValueT> lift(
            final PredictFn<ModelT, InputT, VectorT, ValueT> fn) {
      return (model, vectors) ->
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return fn.apply(model, vectors);
                } catch (final Exception e) {
                  throw new RuntimeException(e.getCause());
                }
              });
    }

    /**
     * The functional interface. Your function/lambda takes model and features after extractions as
     * input, should perform a asynchronous prediction and return the "future" of predictions.
     *
     * <p>Note: if you have a synchronous implementation of prediction function you can use {@link
     * AsyncPredictFn#lift} to make it asynchronous.
     *
     * @param model model to perform prediction on.
     * @param vectors extracted features.
     * @return {@link CompletionStage} of predictions ({@link Prediction}).
     */
    CompletionStage<List<Prediction<InputT, ValueT>>> apply(
        ModelT model, List<Vector<InputT, VectorT>> vectors);

    default <C extends AsyncPredictFn<ModelT, InputT, VectorT, ValueT>> C with(
        final Function<AsyncPredictFn<ModelT, InputT, VectorT, ValueT>, C> fn) {
      return fn.apply(this);
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
    List<Prediction<InputT, ValueT>> apply(ModelT model, List<Vector<InputT, VectorT>> vectors)
        throws Exception;

    default <C extends PredictFn<ModelT, InputT, VectorT, ValueT>> C with(
        final Function<PredictFn<ModelT, InputT, VectorT, ValueT>, C> fn) {
      return fn.apply(this);
    }
  }
}
