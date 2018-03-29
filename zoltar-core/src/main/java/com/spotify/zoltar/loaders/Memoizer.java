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
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Memoizes the result of the supplied {@link ModelLoader}.
 *
 * @param <M> Model instance type.
 */
@FunctionalInterface
public interface Memoizer<M extends Model<?>> extends ModelLoader<M> {

  /**
   * Creates a memoized model loader.
   *
   * @param loader ModelLoader to be memoized.
   * @param <M> Model instance type.
   * @return Memoized loader.
   */
  static <M extends Model<?>> Memoizer<M> memoize(final ModelLoader<M> loader) {
    final AtomicReference<CompletionStage<M>> value = new AtomicReference<>();
    return () -> {
      CompletionStage<M> val = value.get();
      if (val == null) {
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
