/*-
 * -\-\-
 * model-serving-core
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

/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Streams;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.Model.PredictFns.AsyncPredictFn;
import com.spotify.modelserving.Model.PredictFns.PredictFn;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public interface Model<UnderlyingT, SpecT> extends AutoCloseable {

  interface PredictFns {

    @FunctionalInterface
    interface AsyncPredictFn<ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> {

      @SuppressWarnings("checkstyle:LineLength")
      static <ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> AsyncPredictFn<ModelT, SpecT, VectorT, ValueT> lift(
          PredictFn<ModelT, SpecT, VectorT, ValueT> fn) {
        return (model, vectors) -> CompletableFuture.supplyAsync(() -> {
          try {
            return fn.apply(model, vectors);
          } catch (Exception e) {
            throw new RuntimeException(e.getCause());
          }
        });
      }

      CompletionStage<List<Prediction<SpecT, ValueT>>> apply(ModelT model,
                                                             List<Vector<SpecT, VectorT>> vectors);
    }

    @FunctionalInterface
    interface PredictFn<ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> {

      List<Prediction<SpecT, ValueT>> apply(ModelT model, List<Vector<SpecT, VectorT>> vectors)
          throws Exception;

    }
  }

  @FunctionalInterface
  interface Predictor<SpecT, ValueT> {

    ScheduledExecutorService SCHEDULER =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    static <ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> Predictor<SpecT, ValueT> create(
        ModelT model,
        FeatureExtractFn<SpecT, VectorT> featureExtractFn,
        PredictFn<ModelT, SpecT, VectorT, ValueT> predictFn) {
      return create(model, featureExtractFn, AsyncPredictFn.lift(predictFn));
    }

    static <ModelT extends Model<?, SpecT>, SpecT, VectorT, ValueT> Predictor<SpecT, ValueT> create(
        ModelT model,
        FeatureExtractFn<SpecT, VectorT> featureExtractFn,
        AsyncPredictFn<ModelT, SpecT, VectorT, ValueT> predictFn) {
      return (input, timeout, scheduler) -> {
        final List<Vector<SpecT, VectorT>> vectors = FeatureExtractor
            .create(model, featureExtractFn)
            .extract(input);

        final CompletableFuture<List<Prediction<SpecT, ValueT>>> future =
            predictFn.apply(model, vectors).toCompletableFuture();

        ScheduledFuture<?> schedule = scheduler.schedule(() -> {
          future.completeExceptionally(new TimeoutException());
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);

        future.whenComplete((r, t) -> schedule.cancel(true));

        return future;
      };
    }

    CompletionStage<List<Prediction<SpecT, ValueT>>> predict(List<SpecT> input,
                                                             Duration timeout,
                                                             ScheduledExecutorService scheduler)
        throws Exception;

    default CompletionStage<List<Prediction<SpecT, ValueT>>> predict(List<SpecT> input,
                                                                     Duration timeout)
        throws Exception {
      return predict(input, timeout, SCHEDULER);
    }

    default CompletionStage<List<Prediction<SpecT, ValueT>>> predict(List<SpecT> input)
        throws Exception {
      return predict(input, Duration.ofDays(Integer.MAX_VALUE), SCHEDULER);
    }

  }

  @FunctionalInterface
  interface FeatureExtractFn<SpecT, ValueT> {

    List<ValueT> apply(JFeatureExtractor<SpecT> fn) throws Exception;
  }

  @FunctionalInterface
  interface FeatureExtractor<SpecT, ValueT> {

    static <SpecT, ValueT> FeatureExtractor<SpecT, ValueT> create(
        Model<?, SpecT> model,
        FeatureExtractFn<SpecT, ValueT> fn) {
      return inputs -> {
        final JFeatureExtractor<SpecT> extractor = model.featureSpec()
            .extractWithSettings(inputs, model.settings());

        return Streams.zip(inputs.stream(), fn.apply(extractor).stream(), Vector::create)
            .collect(Collectors.toList());
      };
    }

    List<Vector<SpecT, ValueT>> extract(List<SpecT> input) throws Exception;
  }

  @AutoValue
  abstract class Vector<SpecT, ValueT> {

    public abstract SpecT input();

    public abstract ValueT value();

    public static <SpecT, ValueT> Vector<SpecT, ValueT> create(SpecT input, ValueT value) {
      return new AutoValue_Model_Vector<>(input, value);
    }
  }

  @AutoValue
  abstract class Prediction<SpecT, ValueT> {

    public abstract SpecT input();

    public abstract ValueT value();

    public static <SpecT, ValueT> Prediction<SpecT, ValueT> create(SpecT input, ValueT value) {
      return new AutoValue_Model_Prediction<>(input, value);
    }
  }

  UnderlyingT instance();

  String settings();

  JFeatureSpec<SpecT> featureSpec();

}
