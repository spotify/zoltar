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

package com.spotify.zoltar.loaders;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * PreLoader is model loader that calls {@link ModelLoader#get()} allowing model preloading.
 *
 * @param <M> Model instance type.
 */
@FunctionalInterface
public interface PreLoader<M extends Model<?>> extends ModelLoader<M> {

  /**
   * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration} is
   * met.
   *
   * @param supplier model supplier.
   * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
   * @param executor the executor to use for asynchronous execution.
   * @param <M>      Underlying model instance.
   */
  static <M extends Model<?>> ModelLoader<M> preload(final ThrowableSupplier<M> supplier,
                                                     final Duration duration,
                                                     final Executor executor)
      throws InterruptedException, ExecutionException, TimeoutException {
    return preload(ModelLoader.load(supplier, executor), duration);
  }

  /**
   * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration} is
   * met.
   *
   * @param loader model loader.
   * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
   * @param <M>    Underlying model instance.
   */
  static <M extends Model<?>> PreLoader<M> preload(final ModelLoader<M> loader,
                                                   final Duration duration)
      throws InterruptedException, ExecutionException, TimeoutException {
    return ModelLoader.loaded(loader.get(duration))::get;
  }

  /**
   * Returns a blocking {@link PreLoader}. Blocks till the model is loaded or a {@link Duration} is
   * met.
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
