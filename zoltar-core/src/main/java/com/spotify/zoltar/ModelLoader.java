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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Handles the model loading logic.
 *
 * @param <M> Underlying model instance.
 */
@FunctionalInterface
public interface ModelLoader<M extends Model<?>> {

  /**
   * Supplier of {@link Model}. Can throw {@link Exception} when the supplier is invoked.
   */
  @FunctionalInterface
  interface ThrowableSupplier<M extends Model<?>> {

    M get() throws Exception;

  }

  /**
   * Lifts a supplier into a {@link ModelLoader}.
   *
   * @param supplier model supplier.
   * @param <M> Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> lift(final ThrowableSupplier<M> supplier) {
    return () -> CompletableFuture.supplyAsync(() -> {
      try {
        return supplier.get();
      } catch (Exception e) {
        throw new CompletionException(e);
      }
    });
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
  default M get(Duration duration) throws InterruptedException,
                                          ExecutionException,
                                          TimeoutException {
    return get()
        .toCompletableFuture()
        .get(duration.toMillis(), TimeUnit.MILLISECONDS);
  }

  default <L extends ModelLoader<M>> L with(Function<ModelLoader<M>, L> fn) {
    return fn.apply(this);
  }

}
