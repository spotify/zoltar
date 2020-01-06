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

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;

/**
 * Memoizes the result of the supplied {@link ModelLoader}.
 *
 * @param <M> Model instance type.
 */
@Deprecated
@FunctionalInterface
public interface ModelMemoizer<M extends Model<?>> extends ModelLoader<M> {

  /**
   * Creates a memoized model loader.
   *
   * @param loader ModelLoader to be memoized.
   * @param <M> Model instance type.
   * @return Memoized loader.
   */
  static <M extends Model<?>> ModelMemoizer<M> memoize(final ModelLoader<M> loader) {
    // AtomicReference can be updated atomically and offers volatile write/read semantics.
    // However, in this case it's just being used as a container for the CompletionStage.
    final AtomicReference<CompletionStage<M>> value = new AtomicReference<>();
    return () -> {
      CompletionStage<M> val = value.get();
      if (val == null) {
        // we want to avoid .get() being called several times from the different threads
        // because it can be very expensive.
        synchronized (value) {
          val = value.get();
          if (val == null) {
            val = Objects.requireNonNull(loader.get());
            value.set(val);
          }
        }
      }

      return val;
    };
  }
}
