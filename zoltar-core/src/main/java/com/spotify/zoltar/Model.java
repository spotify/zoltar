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

import com.google.auto.value.AutoValue;

/**
 * Model interface. In most cases you can just use the prebaked implementations.
 *
 * @param <UnderlyingT> the underlying type of the model.
 */
public interface Model<UnderlyingT> extends AutoCloseable {

  /** value class to define model id. */
  @AutoValue
  abstract class Id {
    public abstract String value();

    public static Id create(final String value) {
      return new AutoValue_Model_Id(value);
    }
  }

  Id id();

  /**
   * Returns an instance of the underlying model. This could be for example TensorFlow's graph,
   * session or XGBoost's booster.
   */
  UnderlyingT instance();
}
