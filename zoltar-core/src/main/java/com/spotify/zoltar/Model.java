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

public interface Model<UnderlyingT> extends AutoCloseable {

  interface PredictFns {

    @FunctionalInterface
    interface AsyncPredictFn<ModelT extends Model<?>, InputT, VectorT, ValueT> {

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

      CompletionStage<List<Prediction<InputT, ValueT>>> apply(ModelT model,
                                                              List<Vector<InputT, VectorT>> vectors);
    }

    @FunctionalInterface
    interface PredictFn<ModelT extends Model<?>, InputT, VectorT, ValueT> {

      List<Prediction<InputT, ValueT>> apply(ModelT model, List<Vector<InputT, VectorT>> vectors)
          throws Exception;

    }
  }

  @FunctionalInterface
  interface Predictor<InputT, ValueT> {

    ScheduledExecutorService SCHEDULER =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    static <ModelT extends Model<?>, InputT, VectorT, ValueT> Predictor<InputT, ValueT> create(
        final ModelT model,
        final FeatureExtractor<InputT, VectorT> featureExtractor,
        final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
      return create(model, featureExtractor, AsyncPredictFn.lift(predictFn));
    }

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

    CompletionStage<List<Prediction<InputT, ValueT>>> predict(List<InputT> input,
                                                              Duration timeout,
                                                              ScheduledExecutorService scheduler)
        throws Exception;

    default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final List<InputT> input,
                                                                      final Duration timeout)
        throws Exception {
      return predict(input, timeout, SCHEDULER);
    }

    default CompletionStage<List<Prediction<InputT, ValueT>>> predict(final List<InputT> input)
        throws Exception {
      return predict(input, Duration.ofDays(Integer.MAX_VALUE), SCHEDULER);
    }

  }

  interface FeatureExtractFns {

    @FunctionalInterface
    interface ExtractFn<InputT, ValueT> {

      List<ValueT> apply(List<InputT> inputs) throws Exception;
    }

    @FunctionalInterface
    interface FeatranExtractFn<InputT, ValueT> {

      JRecordExtractor<InputT, ValueT> apply(JFeatureSpec<InputT> spec, String settings);
    }
  }

  @FunctionalInterface
  interface FeatureExtractor<InputT, ValueT> {

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

    static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
        final FeatureSpec<InputT> featureSpec,
        final String settings,
        final FeatranExtractFn<InputT, ValueT> fn) {
      return create(JFeatureSpec.wrap(featureSpec), settings, fn);
    }

    static <InputT, ValueT> FeatureExtractor<InputT, ValueT> create(
        final JFeatureSpec<InputT> featureSpec,
        final String settings,
        final FeatranExtractFn<InputT, ValueT> fn) {
      final JRecordExtractor<InputT, ValueT> extractor = fn.apply(featureSpec, settings);
      return inputs -> inputs.stream()
          .map(i -> Vector.create(i, extractor.featureValue(i)))
          .collect(Collectors.toList());
    }

    List<Vector<InputT, ValueT>> extract(List<InputT> input) throws Exception;
  }

  @AutoValue
  abstract class Vector<InputT, ValueT> {

    public abstract InputT input();

    public abstract ValueT value();

    public static <InputT, ValueT> Vector<InputT, ValueT> create(final InputT input,
                                                                 final ValueT value) {
      return new AutoValue_Model_Vector<>(input, value);
    }
  }

  @AutoValue
  abstract class Prediction<InputT, ValueT> {

    public abstract InputT input();

    public abstract ValueT value();

    public static <InputT, ValueT> Prediction<InputT, ValueT> create(final InputT input,
                                                                     final ValueT value) {
      return new AutoValue_Model_Prediction<>(input, value);
    }
  }

  UnderlyingT instance();

}
