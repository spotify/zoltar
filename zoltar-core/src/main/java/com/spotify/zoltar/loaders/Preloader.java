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
package com.spotify.zoltar.loaders;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;

/**
 * Preloader is model loader that calls {@link ModelLoader#get()} allowing model preloading.
 *
 * @param <M> Model instance type.
 */
@Deprecated
@FunctionalInterface
public interface Preloader<M extends Model<?>> extends ModelLoader<M> {

  /** Returns a blocking {@link Preloader}. Blocks at create time till the model is loaded. */
  static <M extends Model<?>> Function<ModelLoader<M>, Preloader<M>> preload() {
    return preload(Duration.ofDays(Integer.MAX_VALUE));
  }

  /**
   * Returns a blocking {@link Preloader}. Blocks till the model is loaded or a {@link Duration} is
   * met.
   *
   * @param duration Amount of time that it should wait, if necessary, for model to be loaded.
   */
  static <M extends Model<?>> Function<ModelLoader<M>, Preloader<M>> preload(
      final Duration duration) {
    return loader -> {
      CompletionStage<M> model;
      try {
        model = CompletableFuture.completedFuture(loader.get(duration));
      } catch (final Exception e) {
        final CompletableFuture<M> failed = new CompletableFuture<>();
        failed.completeExceptionally(e);

        model = failed;
      }

      final CompletionStage<M> finalModel = model;
      return () -> finalModel;
    };
  }

  /** Returns a asynchronous {@link Preloader}. */
  static <M extends Model<?>> Function<ModelLoader<M>, Preloader<M>> preloadAsync() {
    return loader -> {
      final CompletionStage<M> model = loader.get();
      return () -> model;
    };
  }
}
