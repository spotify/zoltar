/*-
 * -\-\-
 * zoltar-models
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

import com.spotify.zoltar.loaders.Memoizer;
import com.spotify.zoltar.tf.TensorFlowGraphModel;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.xgboost.XGBoostModel;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import org.tensorflow.Graph;
import org.tensorflow.framework.ConfigProto;

public final class ModelLoaders {

  /**
   * Returns a XGBoost model given the serialized model stored in the model URI.
   *
   * @param modelUri should point to serialized XGBoost model file, can be a URI to a local
   *                 filesystem, resource, GCS etc.
   */
  public static ModelLoader<XGBoostModel> xgboost(final String modelUri) {
    return Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return XGBoostModel.create(URI.create(modelUri));
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });
  }

  /**
   * Returns a TensorFlow model based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   */
  public static ModelLoader<TensorFlowModel> tensorFlow(final String modelUri) {
    return Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowModel.create(URI.create(modelUri));
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });
  }

  /**
   * Returns a TensorFlow model based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param options  TensorFlow options, see {@link TensorFlowModel.Options}.
   */
  public static ModelLoader<TensorFlowModel> tensorFlow(final String modelUri,
                                                        final TensorFlowModel.Options options) {
    return Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowModel.create(URI.create(modelUri), options);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });
  }

  /**
   * Returns a TensorFlow model based on a serialized TensorFlow {@link Graph}.
   *
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *                 local filesystem, resource, GCS etc.
   * @param config   optional TensorFlow {@link ConfigProto} config.
   * @param prefix   optional prefix that will be prepended to names in the graph.
   */
  public static ModelLoader<TensorFlowGraphModel> tensorFlowGraph(
      final String modelUri,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowGraphModel.create(URI.create(modelUri), config, prefix);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });
  }

  /**
   * Returns a TensorFlow model based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config   optional TensorFlow {@link ConfigProto} config.
   * @param prefix   optional prefix that will be prepended to names in the graph.
   */
  public static ModelLoader<TensorFlowGraphModel> tensorFlowGraph(
      final byte[] graphDef,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return TensorFlowGraphModel.create(graphDef, config, prefix);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });
  }

}
