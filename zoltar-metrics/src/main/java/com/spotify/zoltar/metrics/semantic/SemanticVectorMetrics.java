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

import java.util.List;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;

import com.spotify.zoltar.Vector;
import com.spotify.zoltar.metrics.VectorMetrics;

/** Semantic metric implementation for the {@link VectorMetrics}. */
@AutoValue
public abstract class SemanticVectorMetrics<InputT, ValueT>
    implements VectorMetrics<InputT, ValueT> {

  abstract Timer.Context extractDurationTimer();

  abstract Meter extractRateCounter();

  static <InputT, ValueT> SemanticVectorMetrics<InputT, ValueT> create(
      final Timer.Context extractDurationTime, final Meter extractRateCounter) {
    return new AutoValue_SemanticVectorMetrics<>(extractDurationTime, extractRateCounter);
  }

  @Override
  public void extraction(final List<Vector<InputT, ValueT>> vectors) {
    extractDurationTimer().stop();
    extractRateCounter().mark(vectors.size());
  }
}
