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

package com.spotify.zoltar.models;

import com.spotify.zoltar.tf.TensorFlowGraphModel;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.xgboost.XGBoostModel;
import java.io.IOException;
import java.net.URI;
import javax.annotation.Nullable;
import org.tensorflow.Graph;
import org.tensorflow.framework.ConfigProto;

/**
 * This class consists exclusively of static methods that return Models.
 *
 * This is the public entry point for get get a Model.
 */
public final class Models {

  // Suppresses default constructor, ensuring non-instantiability.
  private Models() {
  }

  /**
   * Returns a XGBoost model given the serialized model stored in the model URI.
   *
   * @param modelUri should point to serialized XGBoost model file, can be a URI to a local
   *                 filesystem, resource, GCS etc.
   */
  public static XGBoostModel xgboost(final String modelUri) throws IOException {
    return XGBoostModel.create(URI.create(modelUri));
  }

  /**
   * Returns a TensorFlow model based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow
   *                 {@link org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem,
   *                 resource, GCS etc.
   */
  public static TensorFlowModel tensorFlow(final String modelUri) throws IOException {
    return TensorFlowModel.create(URI.create(modelUri));
  }

  /**
   * Returns a TensorFlow model based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow
   *                 {@link org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem,
   *                 resource, GCS etc.
   * @param options TensorFlow options, see {@link TensorFlowModel.Options}.
   */
  public static TensorFlowModel tensorFlow(final String modelUri,
                                           final TensorFlowModel.Options options)
      throws IOException {
    return TensorFlowModel.create(URI.create(modelUri), options);
  }

  /**
   * Returns a TensorFlow model based on a serialized TensorFlow {@link Graph}.
   *
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *                 local filesystem, resource, GCS etc.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphModel tensorFlowGraph(final String modelUri,
                                                     @Nullable final ConfigProto config,
                                                     @Nullable final String prefix)
      throws IOException {
    return TensorFlowGraphModel.from(URI.create(modelUri), config, prefix);
  }

  /**
   * Returns a TensorFlow model based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphModel tensorFlowGraph(final byte[] graphDef,
                                                     @Nullable final ConfigProto config,
                                                     @Nullable final String prefix)
      throws IOException {
    return TensorFlowGraphModel.from(graphDef, config, prefix);
  }
}
