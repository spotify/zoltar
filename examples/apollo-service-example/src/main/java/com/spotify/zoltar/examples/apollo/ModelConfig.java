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
package com.spotify.zoltar.examples.apollo;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;

/** Immutable object that contains model / feature extraction configuration properties. */
@AutoValue
public abstract class ModelConfig {

  /** model URI path. */
  public abstract URI modelUri();

  /** settings URI path. */
  public abstract URI settingsUri();

  /** Creates a {@link ModelConfig} create a {@link Config}. */
  public static ModelConfig from(final Config config) throws URISyntaxException {
    URI modelUri = URI.create(config.getString("model"));

    if (modelUri.getScheme() == null) {
      modelUri = ModelConfig.class.getResource(config.getString("model")).toURI();
    }

    URI settingsUri = URI.create(config.getString("settings"));
    if (settingsUri.getScheme() == null) {
      settingsUri = ModelConfig.class.getResource(config.getString("settings")).toURI();
    }

    return create(modelUri, settingsUri);
  }

  public static ModelConfig create(final URI modelUri, final URI settingsUri) {
    return new AutoValue_ModelConfig(modelUri, settingsUri);
  }
}
