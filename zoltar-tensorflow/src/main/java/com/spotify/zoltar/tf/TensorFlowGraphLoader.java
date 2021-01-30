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
package com.spotify.zoltar.tf;

import java.net.URI;

import javax.annotation.Nullable;

import org.tensorflow.Graph;
import org.tensorflow.proto.framework.ConfigProto;
import org.tensorflow.proto.framework.GraphDef;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;

/** {@link TensorFlowGraphModel} loader. */
@FunctionalInterface
public interface TensorFlowGraphLoader extends ModelLoader<TensorFlowGraphModel> {

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param modelUri should point to a serialized TensorFlow {@link org.tensorflow.Graph} file on
   *     local filesystem, resource, GCS etc.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(
      final String modelUri, @Nullable final ConfigProto config, @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(URI.create(modelUri), config, prefix));
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
  static TensorFlowGraphLoader create(
      final Model.Id id,
      final String modelUri,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(id, URI.create(modelUri), config, prefix));
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(
      final byte[] graphDef, @Nullable final ConfigProto config, @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(graphDef, config, prefix));
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(
      final GraphDef graphDef, @Nullable final ConfigProto config, @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(graphDef, config, prefix));
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param id model id @{link Model.Id}.
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(
      final Model.Id id,
      final byte[] graphDef,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(id, graphDef, config, prefix));
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param id model id @{link Model.Id}.
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config optional TensorFlow {@link ConfigProto} config.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  static TensorFlowGraphLoader create(
      final Model.Id id,
      final GraphDef graphDef,
      @Nullable final ConfigProto config,
      @Nullable final String prefix) {
    return create(() -> TensorFlowGraphModel.create(id, graphDef, config, prefix));
  }

  /**
   * Returns a TensorFlow model loader based on a serialized TensorFlow {@link Graph}.
   *
   * @param supplier {@link TensorFlowGraphModel} supplier.
   */
  static TensorFlowGraphLoader create(final ThrowableSupplier<TensorFlowGraphModel> supplier) {
    return ModelLoader.load(supplier)::get;
  }
}
