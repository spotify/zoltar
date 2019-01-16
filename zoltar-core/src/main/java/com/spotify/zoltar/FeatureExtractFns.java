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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Feature extraction functions. Functions used to transform raw input into extracted features,
 * should be used together with {@link FeatureExtractor}.
 *
 * @see FeatureExtractor
 */
public interface FeatureExtractFns {

  /**
   * Generic feature extraction function, takes multiple raw input and should return extracted
   * features of user defined type.
   *
   * @param <InputT> type of the input to feature extraction.
   * @param <VectorT> type of feature extraction result.
   */
  @FunctionalInterface
  interface ExtractFn<InputT, VectorT> {

    @FunctionalInterface
    interface UnaryExtractFn<InputT, VectorT> extends Function<InputT, VectorT> {}

    @FunctionalInterface
    interface BatchExtractFn<InputT, VectorT>
        extends Function<List<InputT>, List<Vector<InputT, VectorT>>> {

      static <InputT, VectorT> BatchExtractFn<InputT, VectorT> batch(
          UnaryExtractFn<InputT, VectorT> fn) {
        return inputs -> {
          return inputs
              .stream()
              .map(i -> Vector.create(i, fn.apply(i)))
              .collect(Collectors.toList());
        };
      }
    }

    @FunctionalInterface
    interface ConsExtractFn<InputT, VectorT> extends ExtractFn<InputT, VectorT> {
      static <InputT, VectorT> ConsExtractFn<InputT, VectorT> cons(
          final List<Vector<InputT, VectorT>> vectors) {
        final CompletionStage<List<Vector<InputT, VectorT>>> future =
            CompletableFuture.completedFuture(vectors);
        return () -> future;
      }

      CompletionStage<List<Vector<InputT, VectorT>>> get();

      @Override
      default CompletionStage<List<Vector<InputT, VectorT>>> apply(List<InputT> inputs) {
        return get();
      }
    }

    @FunctionalInterface
    interface TimeoutExtractFn<InputT, VectorT> extends ExtractFn<InputT, VectorT> {

      static <InputT, VectorT> TimeoutExtractFn<InputT, VectorT> timeout(
          final ExtractFn<InputT, VectorT> fn, final Duration duration, final Executor executor) {
        return ExtractFn.extract(
                (BatchExtractFn<InputT, VectorT>)
                    inputs -> {
                      try {
                        return fn.apply(inputs)
                            .toCompletableFuture()
                            .get(duration.toMillis(), TimeUnit.MILLISECONDS);
                      } catch (Exception e) {
                        throw new CompletionException(e);
                      }
                    },
                executor)
            ::apply;
      }
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extracted(
        final List<Vector<InputT, VectorT>> vectors) {
      return ConsExtractFn.cons(vectors);
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final UnaryExtractFn<InputT, VectorT> fn) {
      return extract(BatchExtractFn.batch(fn));
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final BatchExtractFn<InputT, VectorT> fn) {
      return fn.andThen(CompletableFuture::completedFuture)::apply;
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final UnaryExtractFn<InputT, VectorT> fn, final Executor executor) {
      return extract(BatchExtractFn.batch(fn), executor);
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final UnaryExtractFn<InputT, VectorT> fn,
        final Duration duration,
        final Executor executor) {
      return extract(BatchExtractFn.batch(fn), duration, executor);
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final BatchExtractFn<InputT, VectorT> fn, final Executor executor) {
      return inputs -> CompletableFuture.supplyAsync(() -> fn.apply(inputs), executor);
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> extract(
        final BatchExtractFn<InputT, VectorT> fn,
        final Duration duration,
        final Executor executor) {
      return timeout(extract(fn, executor), duration, executor);
    }

    static <InputT, VectorT> ExtractFn<InputT, VectorT> timeout(
        final ExtractFn<InputT, VectorT> fn, final Duration duration, final Executor executor) {
      return TimeoutExtractFn.timeout(fn, duration, executor);
    }

    /** Functional interface. Perform feature extraction. */
    CompletionStage<List<Vector<InputT, VectorT>>> apply(List<InputT> inputs);

    static <InputT> ExtractFn<InputT, InputT> identity() {
      return inputs -> {
        final List<Vector<InputT, InputT>> vectors =
            inputs.stream().map(i -> Vector.create(i, i)).collect(Collectors.toList());
        return ConsExtractFn.cons(vectors).get();
      };
    }

    default <C extends ExtractFn<InputT, VectorT>> C with(
        final Function<ExtractFn<InputT, VectorT>, C> fn) {
      return fn.apply(this);
    }
  }
}
