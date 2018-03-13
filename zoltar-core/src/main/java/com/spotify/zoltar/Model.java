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

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import com.spotify.zoltar.Model.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.Model.FeatureExtractFns.FeatranExtractFn;
import com.spotify.zoltar.Model.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Model.PredictFns.PredictFn;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Model interface. In most cases you can just use the prebaked implementations.
 *
 * @param <UnderlyingT> the underlying type of the model.
 */
public interface Model<UnderlyingT> extends AutoCloseable {

  /**
   * Prediction function interfaces. Describe how to perform predictions on a model given input
   * vectors. See prebaked implementations for prebaked models.
   *
   * @see Predictor
   */
  interface PredictFns {

    /**
     * Asynchronous prediction functional interface. Allows to define prediction function via
     * lambda, prediction is happens asynchronously.
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
      static <ModelT extends Model<?>, InputT, VectorT, ValueT> AsyncPredictFn<ModelT, InputT, VectorT, ValueT> lift(
          final PredictFn<ModelT, InputT, VectorT, ValueT> fn) {
        return (model, vectors) -> CompletableFuture.supplyAsync(() -> {
          try {
            return fn.apply(model, vectors);
          } catch (final Exception e) {
            throw new RuntimeException(e.getCause());
          }
        });
      }

      /**
       * The functional interface. Your function/lambda takes model and features after extractions
       * as input, should perform a asynchronous prediction and return the "future" of predictions.
       *
       * Note: if you have a synchronous implementation of prediction function you can use
       * {@link AsyncPredictFn#lift} to make it asynchronous.
       *
       * @param model model to perform prediction on.
       * @param vectors extracted features.
       * @return {@link CompletionStage} of predictions ({@link Prediction}).
       */
      CompletionStage<List<Prediction<InputT, ValueT>>> apply(ModelT model,
                                                              List<Vector<InputT, VectorT>> vectors);
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
       * The functional interface. Your function/lambda takes model and features after extractions
       * as input, should perform a prediction and return the predictions.
       *
       * @param model model to perform prediction on.
       * @param vectors extracted features.
       * @return predictions ({@link Prediction}).
       */
      List<Prediction<InputT, ValueT>> apply(ModelT model, List<Vector<InputT, VectorT>> vectors)
          throws Exception;

    }
  }

  /**
   * Entry point for prediction, it allows to perform E2E prediction given a recipe made of a
   * {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}. In most cases you should use
   * the static factory methods.
   *
   * @param <InputT> type of the feature extraction input.
   * @param <ValueT> type of the prediction output.
   */
  @FunctionalInterface
  interface Predictor<InputT, ValueT> {

    /** Default scheduler for predict functions. */
    ScheduledExecutorService SCHEDULER =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a {@link PredictFn}.
     *
     * @param model model to perform prediction on.
     * @param featureExtractor a feature extractor to use to transform input into
     *                         extracted features.
     * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
     * @param <ModelT> underlying type of the {@link Model}.
     * @param <InputT> type of the input to the {@link FeatureExtractor}.
     * @param <VectorT> type of the output from {@link FeatureExtractor}.
     * @param <ValueT> type of the prediction result.
     */
    static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
        final ModelT model,
        final FeatureExtractor<InputT, VectorT> featureExtractor,
        final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
      return create(model, featureExtractor, AsyncPredictFn.lift(predictFn));
    }

    /**
     * Returns a predictor given a {@link Model}, {@link FeatureExtractor} and a
     * {@link AsyncPredictFn}.
     *
     * @param model model to perform prediction on.
     * @param featureExtractor a feature extractor to use to transform input into
     *                         extracted features.
     * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
     * @param <ModelT> underlying type of the {@link Model}.
     * @param <InputT> type of the input to the {@link FeatureExtractor}.
     * @param <VectorT> type of the output from {@link FeatureExtractor}.
     * @param <ValueT> type of the prediction result.
     */
    static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
        final ModelT model,
        final FeatureExtractor<InputT, VectorT> featureExtractor,
        final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
      return (input, timeout, scheduler) -> {
        final List<Vector<InputT, VectorT>> vectors = featureExtractor.extract(input);

        final CompletableFuture<List<Prediction<InputT, ValueT>>> future =
            predictFn.apply(model, vectors).toCompletableFuture();

        final ScheduledFuture<?> schedule = scheduler.schedule(() -> {
          future.completeExceptionally(new TimeoutException());
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);

        future.whenComplete((r, t) -> schedule.cancel(true));

        return future;
      };
    }

    /**
     * Functional interface. You should perform E2E feature extraction and prediction.
     * See {@link Predictor#create(Model, FeatureExtractor, AsyncPredictFn)} for an example of
     * usage.
     *
     * @param input a list of inputs to perform feature extraction and prediction on.
     * @param timeout implementation specific timeout,
     *                see {@link Predictor#create(Model, FeatureExtractor, AsyncPredictFn)} for an
     *                example of usage.
     * @param scheduler implementation specific scheduler,
     *                  see {@link Predictor#create(Model, FeatureExtractor, AsyncPredictFn)} for an
     *                  example of usage.
     */
    CompletionStage<List<Prediction<InputT, ValueT>>> predict(List<InputT> input,
                                                              Duration timeout,
                                                              ScheduledExecutorService scheduler)
        throws Exception;

    /**
     * Perform prediction with a default scheduler.
     */
    default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final List<InputT> input,
                                                                      final Duration timeout)
        throws Exception {
      return predict(input, timeout, SCHEDULER);
    }

    /**
     * Perform prediction with a default scheduler, and practically infinite timeout.
     */
    default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final List<InputT> input)
        throws Exception {
      return predict(input, Duration.ofDays(Integer.MAX_VALUE), SCHEDULER);
    }

  }

  /**
   * Feature extraction functions. Functions used to transform raw input into extracted features,
   * should be used together with {@link FeatureExtractor}.
   *
   * @see FeatureExtractor
   */
  interface FeatureExtractFns {

    /**
     * Generic feature extraction function, takes raw input and should return extracted features of
     * user defined type.
     *
     * @param <InputT> type of the input to feature extraction.
     * @param <ValueT> type of feature extraction result.
     */
    @FunctionalInterface
    interface ExtractFn<InputT, ValueT> {

      /**
       * Functional interface. Perform feature extraction.
       */
      List<ValueT> apply(List<InputT> inputs) throws Exception;
    }

    /**
     * <a href="https://github.com/spotify/featran">Featran</a> specific feature extraction
     * function.
     *
     * @param <InputT> type of the input to the {@link JFeatureSpec}.
     * @param <ValueT> type of the output from {@link JRecordExtractor}.
     */
    @FunctionalInterface
    interface FeatranExtractFn<InputT, ValueT> {

      /**
       * Functional interface. Perform feature extraction given Featran's feature specification and
       * settings.
       *
       * @param spec Featran's feature spec.
       * @param settings Featran's settings.
       * @return
       */
      JRecordExtractor<InputT, ValueT> apply(JFeatureSpec<InputT> spec, String settings);
    }
  }

  /**
   * Functional interface for feature extraction. Should be used together with {@link Predictor}.
   * In most cases you should use the static factory methods.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <ValueT> type of feature extraction result.
   */
  @FunctionalInterface
  interface FeatureExtractor<InputT, ValueT> {

    /**
     * Creates an extractor given a generic {@link ExtractFn}, consider using
     * <a href="https://github.com/spotify/featran">Featran</a> and {@link FeatranExtractFn}
     * whenever possible.
     *
     * @param fn {@link ExtractFn} extraction function
     * @param <InputT> type of the input to feature extraction.
     * @param <ValueT> type of feature extraction result.
     */
    static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
        final ExtractFn<InputT, ValueT> fn) {
      return inputs -> {
        final List<Vector<InputT, ValueT>> result = Lists.newArrayList();
        final Iterator<InputT> i1 = inputs.iterator();
        final Iterator<ValueT> i2 = fn.apply(inputs).iterator();
        while (i1.hasNext() && i2.hasNext()) {
          result.add(Vector.create(i1.next(), i2.next()));
        }
        return result;
      };
    }

    /**
     * Creates an Featran based feature extractor.
     *
     * @param featureSpec Featran's {@link FeatureSpec}.
     * @param settings Featran's settings.
     * @param fn {@link FeatranExtractFn} function, for example
     *           {@link JFeatureSpec#extractWithSettingsExample}
     * @param <InputT> type of the input to the {@link FeatureSpec}.
     * @param <ValueT> type of the output from {@link FeatranExtractFn}.
     */
    static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
        final FeatureSpec<InputT> featureSpec,
        final String settings,
        final FeatranExtractFn<InputT, ValueT> fn) {
      return create(JFeatureSpec.wrap(featureSpec), settings, fn);
    }

    /**
     * Creates an Featran based feature extractor.
     *
     * @param featureSpec Featran's {@link JFeatureSpec}.
     * @param settings Featran's settings.
     * @param fn {@link FeatranExtractFn} function, for example
     *           {@link JFeatureSpec#extractWithSettingsExample}
     * @param <InputT> type of the input to the {@link FeatureSpec}.
     * @param <ValueT> type of the output from {@link FeatranExtractFn}.
     */
    static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
        final JFeatureSpec<InputT> featureSpec,
        final String settings,
        final FeatranExtractFn<InputT, ValueT> fn) {
      final JRecordExtractor<InputT, ValueT> extractor = fn.apply(featureSpec, settings);
      return inputs -> inputs.stream()
          .map(i -> Vector.create(i, extractor.featureValue(i)))
          .collect(Collectors.toList());
    }

    /**
     * Functional interface. Perform the feature extraction given the input.
     */
    List<Vector<InputT, ValueT>> extract(List<InputT> input) throws Exception;
  }

  /**
   * Value class for feature extraction result. Holds both the original input and the result of the
   * feature extraction for the input.
   */
  @AutoValue
  abstract class Vector<InputT, ValueT> {

    /** Input to the feature extraction. */
    public abstract InputT input();

    /** Result of the feature extraction. */
    public abstract ValueT value();

    /** Create a new feature extraction result. */
    public static <InputT, ValueT> Vector<InputT, ValueT> create(final InputT input,
                                                                 final ValueT value) {
      return new AutoValue_Model_Vector<>(input, value);
    }
  }

  /**
   * Value class for prediction result. Holds both the original input and the result of the
   * prediction for the input.
   */
  @AutoValue
  abstract class Prediction<InputT, ValueT> {

    /** Input to the prediction. */
    public abstract InputT input();

    /** Result of the prediction. */
    public abstract ValueT value();

    /** Create a new prediction result. */
    public static <InputT, ValueT> Prediction<InputT, ValueT> create(final InputT input,
                                                                     final ValueT value) {
      return new AutoValue_Model_Prediction<>(input, value);
    }
  }

  /**
   * Returns an instance of the underlying model. This could be for example TensorFlow's graph,
   * session or XGBoost's booster.
   */
  UnderlyingT instance();

}
