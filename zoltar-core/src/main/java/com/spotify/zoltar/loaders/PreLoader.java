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

@FunctionalInterface
public interface PreLoader<M extends Model<?>> extends ModelLoader<M> {

  /**
   * Creates a non blocking preload model loader that calls {@link ModelLoader#get()}
   * at creation time.
   *
   * @param loader ModelLoader to be preloaded.
   * @param <M> Model instance type.
   */
  static <M extends Model<?>> PreLoader<M> preload(final ModelLoader<M> loader) {
    loader.get();

    return loader::get;
  }

}
