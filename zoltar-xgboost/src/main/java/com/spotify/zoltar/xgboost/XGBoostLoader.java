/*-
 * -\-\-
 * zoltar-xgboost
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

package com.spotify.zoltar.xgboost;

import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.loaders.ModelMemoizer;
import com.spotify.zoltar.loaders.Preloader;
import java.net.URI;

/**
 * {@link XGBoostModel} loader. This loader is composed with {@link ModelMemoizer}
 * and {@link Preloader}.
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface XGBoostLoader extends ModelLoader<XGBoostModel> {

  /**
   * Returns a XGBoost model loader given the serialized model stored in the model URI.
   *
   * @param modelUri should point to serialized XGBoost model file, can be a URI to a local
   *                 filesystem, resource, GCS etc.
   */
  static XGBoostLoader create(final String modelUri) {
    final ModelLoader<XGBoostModel> loader = ModelLoader
        .lift(() -> XGBoostModel.create(URI.create(modelUri)))
        .with(ModelMemoizer::memoize)
        .with(Preloader.preload());

    return loader::get;
  }

}
