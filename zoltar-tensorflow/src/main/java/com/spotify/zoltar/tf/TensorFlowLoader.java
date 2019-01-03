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

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import java.net.URI;
import java.util.concurrent.Executor;

/**
 * {@link TensorFlowModel} loader.
 */
@FunctionalInterface
public interface TensorFlowLoader extends ModelLoader<TensorFlowModel> {

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param executor the executor to use for asynchronous execution.
   */
  static TensorFlowLoader create(final String modelUri, final Executor executor) {
    return create(() -> TensorFlowModel.create(URI.create(modelUri)), executor);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param executor the executor to use for asynchronous execution.
   */
  static TensorFlowLoader create(final Model.Id id,
                                 final String modelUri,
                                 final Executor executor) {
    return create(() -> TensorFlowModel.create(id, URI.create(modelUri)), executor);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param options  TensorFlow options, see {@link TensorFlowModel.Options}.
   * @param executor the executor to use for asynchronous execution.
   */
  static TensorFlowLoader create(final String modelUri,
                                 final TensorFlowModel.Options options,
                                 final Executor executor) {
    return create(() -> TensorFlowModel.create(URI.create(modelUri), options), executor);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *                 org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource,
   *                 GCS etc.
   * @param options  TensorFlow options, see {@link TensorFlowModel.Options}.
   * @param executor the executor to use for asynchronous execution.
   */
  static TensorFlowLoader create(final Model.Id id,
                                 final String modelUri,
                                 final TensorFlowModel.Options options,
                                 final Executor executor) {
    return create(() -> TensorFlowModel.create(id, URI.create(modelUri), options), executor);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param id model id @{link Model.Id}.
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param options TensorFlow options, see {@link TensorFlowModel.Options}.
   * @param signatureDef name of the signature definition to load from the exported model
   */
  static TensorFlowLoader create(
      final Model.Id id,
      final String modelUri,
      final TensorFlowModel.Options options,
      final String signatureDef,
      final Executor executor) {
    return create(
        () -> TensorFlowModel.create(id, URI.create(modelUri), options, signatureDef), executor);
  }

  /**
   * Returns a TensorFlow model loader based on a saved model.
   *
   * @param supplier {@link TensorFlowModel} supplier.
   * @param executor the executor to use for asynchronous execution.
   */
  static TensorFlowLoader create(final ThrowableSupplier<TensorFlowModel> supplier,
                                 final Executor executor) {
    return ModelLoader.load(supplier, executor)::get;
  }
}
