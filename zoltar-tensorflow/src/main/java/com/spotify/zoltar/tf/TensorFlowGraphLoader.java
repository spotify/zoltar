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
import com.spotify.zoltar.loaders.Memoizer;
import com.spotify.zoltar.loaders.PreLoader;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import org.tensorflow.Graph;
import org.tensorflow.framework.ConfigProto;

@FunctionalInterface
public interface TensorFlowGraphLoader extends ModelLoader<TensorFlowGraphModel> {

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *                 local filesystem, resource, GCS etc.
   * @param config   optional TensorFlow {@link ConfigProto} config.
   * @param prefix   optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(final String modelUri,
                                      @Nullable final ConfigProto config,
                                      @Nullable final String prefix) {
    return PreLoader.preload(Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowGraphModel.create(URI.create(modelUri), config, prefix);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    }))::get;
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config   optional TensorFlow {@link ConfigProto} config.
   * @param prefix   optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(final byte[] graphDef,
                                      @Nullable final ConfigProto config,
                                      @Nullable final String prefix) {
    return PreLoader.preload(Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowGraphModel.create(graphDef, config, prefix);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    }))::get;
  }

}
