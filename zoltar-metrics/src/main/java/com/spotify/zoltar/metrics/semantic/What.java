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
package com.spotify.zoltar.metrics.semantic;

/** Enumerates the metrics tracked, keyed by the 'what' tag. */
public enum What {
  /** Prediction duration. */
  PREDICT_DURATION("predict-duration"),
  /** Prediction rate. */
  PREDICT_RATE("predict-rate"),
  /** Feature Extraction duration. */
  FEATURE_EXTRACT_DURATION("feature-extract-duration"),
  /** Feature Extraction rate. */
  FEATURE_EXTRACT_RATE("feature-extract-rate");

  private final String tag;

  What(final String tag) {
    this.tag = tag;
  }

  public String tag() {
    return tag;
  }
}
