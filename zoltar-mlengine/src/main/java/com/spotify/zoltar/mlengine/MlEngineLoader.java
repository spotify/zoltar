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
package com.spotify.zoltar.mlengine;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;

/** {@link MlEngineLoader} loader. */
@FunctionalInterface
public interface MlEngineLoader extends ModelLoader<MlEngineModel> {

  /**
   * Returns a Google Cloud ML Engine model loader that references the default model.
   *
   * @param projectId Google project id.
   * @param modelId model id.
   */
  static MlEngineLoader create(final String projectId, final String modelId) {
    return create(() -> MlEngineModel.create(projectId, modelId));
  }

  /**
   * Returns a Google Cloud ML Engine model loader.
   *
   * @param projectId Google project id.
   * @param modelId model id.
   * @param versionId model version id.
   */
  static MlEngineLoader create(
      final String projectId, final String modelId, final String versionId) {
    return create(() -> MlEngineModel.create(projectId, modelId, versionId));
  }

  /**
   * Returns a Google Cloud ML Engine model loader.
   *
   * @param id {@link Model.Id} needs to be created with the following format:
   *     <pre>
   *           "projects/{PROJECT_ID}/models/{MODEL_ID}/versions/{MODEL_VERSION}"
   *           </pre>
   */
  static MlEngineLoader create(final Model.Id id) {
    return create(() -> MlEngineModel.create(id));
  }

  /**
   * Returns a Google Cloud ML Engine model loader.
   *
   * @param supplier {@link MlEngineModel} supplier.
   */
  static MlEngineLoader create(final ThrowableSupplier<MlEngineModel> supplier) {
    return ModelLoader.load(supplier)::get;
  }
}
