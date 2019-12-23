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

import java.io.Serializable;

import com.google.auto.value.AutoValue;

/**
 * Value class for feature extraction result. Holds both the original input and the result of the
 * feature extraction for the input.
 */
@AutoValue
public abstract class Vector<InputT, ValueT> implements Serializable {

  /** Input to the feature extraction. */
  public abstract InputT input();

  /** Result of the feature extraction. */
  public abstract ValueT value();

  /** Create a new feature extraction result. */
  public static <InputT, ValueT> Vector<InputT, ValueT> create(
      final InputT input, final ValueT value) {
    return new AutoValue_Vector<>(input, value);
  }
}
