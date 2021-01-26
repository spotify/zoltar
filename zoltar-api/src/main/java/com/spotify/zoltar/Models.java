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
package com.spotify.zoltar;

import javax.annotation.Nullable;

import org.tensorflow.Graph;
import org.tensorflow.proto.framework.ConfigProto;

import com.spotify.zoltar.mlengine.MlEngineLoader;
import com.spotify.zoltar.tf.TensorFlowGraphLoader;
import com.spotify.zoltar.tf.TensorFlowLoader;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.xgboost.XGBoostLoader;

/**
 * This class consists exclusively of static methods that return Model Loaders.
 *
 * <p>This is the public entry point for get a Model.
 */
public final class Models {

  private Models() {}

  /**
   * Returns a XGBoost model loader given the serialized model stored in the model URI.
   *
   * @param modelUri should point to serialized XGBoost model file, can be a URI to a local
   *     filesystem, resource, GCS etc.
   */
  public static XGBoostLoader xgboost(final String modelUri) {
    return XGBoostLoader.create(modelUri);
  }

  /**
   * Returns a XGBoost model loader given the serialized model stored in the model URI.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to serialized XGBoost model file, can be a URI to a local
   *     filesystem, resource, GCS etc.
   */
  public static XGBoostLoader xgboost(final Model.Id id, final String modelUri) {
    return XGBoostLoader.create(id, modelUri);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   */
  public static TensorFlowLoader tensorFlow(final String modelUri) {
    return TensorFlowLoader.create(modelUri);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   */
  public static TensorFlowLoader tensorFlow(final Model.Id id, final String modelUri) {
    return TensorFlowLoader.create(id, modelUri);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param options TensorFlow options, see {@link TensorFlowModel.Options}.
   */
  public static TensorFlowLoader tensorFlow(
      final String modelUri, final TensorFlowModel.Options options) {
    return TensorFlowLoader.create(modelUri, options);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param options TensorFlow options, see {@link TensorFlowModel.Options}.
   */
  public static TensorFlowLoader tensorFlow(
      final Model.Id id, final String modelUri, final TensorFlowModel.Options options) {
    return TensorFlowLoader.create(id, modelUri, options);
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *     local filesystem, resource, GCS etc.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphLoader tensorFlowGraph(
      final String modelUri, @Nullable final ConfigProto config, @Nullable final String prefix) {
    return TensorFlowGraphLoader.create(modelUri, config, prefix);
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *     local filesystem, resource, GCS etc.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphLoader tensorFlowGraph(
      final Model.Id id,
      final String modelUri,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return TensorFlowGraphLoader.create(id, modelUri, config, prefix);
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphLoader tensorFlowGraph(
      final byte[] graphDef, @Nullable final ConfigProto config, @Nullable final String prefix) {
    return TensorFlowGraphLoader.create(graphDef, config, prefix);
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param id model id @{link Model.Id}.
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphLoader tensorFlowGraph(
      final Model.Id id,
      final byte[] graphDef,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return TensorFlowGraphLoader.create(id, graphDef, config, prefix);
  }

  /**
   * Returns a Cloud ML Engine backed model.
   *
   * @param id model id. Id needs to be in the following format: <code>
   *     projects/$projectId/models/$modelId/version/$versionId</code>
   */
  public static MlEngineLoader mlEngine(final Model.Id id) {
    return MlEngineLoader.create(id);
  }

  /**
   * Returns a Cloud Ml Engine backed model.
   *
   * @param projectId Google project id.
   * @param modelId Model id.
   * @param versionId Model version id.
   */
  public static MlEngineLoader mlEngine(
      final String projectId, final String modelId, final String versionId) {
    return MlEngineLoader.create(projectId, modelId, versionId);
  }
}
