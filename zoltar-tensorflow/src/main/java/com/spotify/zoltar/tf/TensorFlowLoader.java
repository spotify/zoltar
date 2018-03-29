/*-
 * -\-\-
 * zoltar-tensorflow
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

package com.spotify.zoltar.tf;

import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.loaders.ModelMemoizer;
import com.spotify.zoltar.loaders.Preloader;
import java.net.URI;

@FunctionalInterface
public interface TensorFlowLoader extends ModelLoader<TensorFlowModel> {

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   */
  static TensorFlowLoader create(final String modelUri) {
    final ModelLoader<TensorFlowModel> loader = ModelLoader
        .lift(() -> TensorFlowModel.create(URI.create(modelUri)))
        .with(ModelMemoizer::memoize)
        .with(Preloader.preloadAsync());

    return loader::get;
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param options  TensorFlow options, see {@link TensorFlowModel.Options}.
   */
  static TensorFlowLoader create(final String modelUri,
                                 final TensorFlowModel.Options options) {
    final ModelLoader<TensorFlowModel> loader = ModelLoader
        .lift(() -> TensorFlowModel.create(URI.create(modelUri), options))
        .with(ModelMemoizer::memoize)
        .with(Preloader.preloadAsync());

    return loader::get;
  }

}
