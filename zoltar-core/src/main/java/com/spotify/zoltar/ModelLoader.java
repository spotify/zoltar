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

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Handles the model loading logic.
 *
 * @param <M> Underlying model instance.
 */
@FunctionalInterface
public interface ModelLoader<M extends Model<?>> extends Closeable {
  /** default executor. */
  ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newWorkStealingPool();

  /** Supplier of {@link Model}. Can throw {@link Exception} when the supplier is invoked. */
  @FunctionalInterface
  interface ThrowableSupplier<M extends Model<?>> {

    M get() throws Exception;
  }

  /**
   * PreLoader is model loader that calls {@link ModelLoader#get()} allowing model preloading.
   *
   * @param <M> Model instance type.
   */
  @FunctionalInterface
  interface PreLoader<M extends Model<?>> extends ModelLoader<M> {

    /**
     * Returns a blocking {@link ModelLoader}. Blocks till the model is loaded or a {@link Duration}
     * is met.
     *
     * @param supplier model supplier.
     * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
     * @param executor the executor to use for asynchronous execution.
     * @param <M> Underlying model instance.
     */
    static <M extends Model<?>> PreLoader<M> preload(
        final ThrowableSupplier<M> supplier, final Duration duration, final Executor executor)
        throws InterruptedException, ExecutionException, TimeoutException {
      return preload(ModelLoader.load(supplier, executor), duration);
    }

    /**
     * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration}
     * is met.
     *
     * @param loader model loader.
     * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
     * @param <M> Underlying model instance.
     */
    static <M extends Model<?>> PreLoader<M> preload(
        final ModelLoader<M> loader, final Duration duration)
        throws InterruptedException, ExecutionException, TimeoutException {
      return ModelLoader.loaded(loader.get(duration))::get;
    }

    /**
     * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration}
     * is met.
     *
     * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
     */
    static <M extends Model<?>> Function<ModelLoader<M>, PreLoader<M>> preload(
        final Duration duration) {
      return loader -> {
        try {
          return preload(loader, duration);
        } catch (final Exception e) {
          final CompletableFuture<M> failed = new CompletableFuture<>();
          failed.completeExceptionally(e);

          return () -> failed;
        }
      };
    }
  }

  /**
   * ConsLoader is a constant {@link ModelLoader}.
   *
   * @param <M> Model instance type.
   */
  @FunctionalInterface
  interface ConsLoader<M extends Model<?>> extends ModelLoader<M> {

    /**
     * Creates a {@link ModelLoader} with an already loaded model.
     *
     * @param model Underlying model instance.
     */
    static <M extends Model<?>> ConsLoader<M> cons(final M model) {
      final CompletableFuture<M> m = CompletableFuture.completedFuture(model);
      return () -> m;
    }
  }

  /**
   * Creates a {@link ModelLoader} with an already loaded model.
   *
   * @param model Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> loaded(final M model) {
    return ConsLoader.cons(model);
  }

  /**
   * Returns a blocking {@link ModelLoader}. Blocks till the model is loaded or a {@link Duration}
   * is met.
   *
   * @param supplier model supplier.
   * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
   * @param executor the executor to use for asynchronous execution.
   * @param <M> Underlying model instance.
   */
  static <M extends Model<?>> PreLoader<M> preload(
      final ThrowableSupplier<M> supplier, final Duration duration, final Executor executor)
      throws InterruptedException, ExecutionException, TimeoutException {
    return PreLoader.preload(supplier, duration, executor);
  }

  /**
   * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration} is
   * met.
   *
   * @param loader model loader.
   * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
   * @param <M> Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> preload(
      final ModelLoader<M> loader, final Duration duration)
      throws InterruptedException, ExecutionException, TimeoutException {
    return PreLoader.preload(loader, duration);
  }

  /**
   * Create a {@link ModelLoader} that loads the supplied model asynchronously.
   *
   * @param supplier model supplier.
   * @param <M> Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> load(
      final ThrowableSupplier<M> supplier, final Executor executor) {
    final CompletableFuture<M> future =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                return supplier.get();
              } catch (final Exception e) {
                throw new CompletionException(e);
              }
            },
            executor);

    return () -> future;
  }

  /**
   * Create a {@link ModelLoader} that loads the supplied model asynchronously.
   *
   * @param supplier model supplier.
   * @param <M> Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> load(final ThrowableSupplier<M> supplier) {
    return load(supplier, DEFAULT_EXECUTOR_SERVICE);
  }

  /**
   * Lifts a supplier into a {@link ModelLoader}.
   *
   * @param supplier model supplier.
   * @param <M> Underlying model instance.
   */
  @Deprecated
  static <M extends Model<?>> ModelLoader<M> lift(final ThrowableSupplier<M> supplier) {
    return load(supplier);
  }

  /**
   * Get's the underlying model instance.
   *
   * @return Model instance
   */
  CompletionStage<M> get();

  /**
   * Get's the underlying model instance.
   *
   * @return Model instance
   */
  default M get(final Duration duration)
      throws InterruptedException, ExecutionException, TimeoutException {
    return get().toCompletableFuture().get(duration.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Deprecated
  default <L extends ModelLoader<M>> L with(final Function<ModelLoader<M>, L> fn) {
    return compose(fn);
  }

  default <L extends ModelLoader<M>> L compose(final Function<ModelLoader<M>, L> fn) {
    return fn.apply(this);
  }

  @Override
  default void close() throws IOException {
    DEFAULT_EXECUTOR_SERVICE.shutdown();
  }
}
