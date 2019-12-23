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
package com.spotify.zoltar.metrics;

import java.util.function.Function;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.PredictorBuilder;

/**
 * This class consists exclusively of static methods that return functions used to decorate types
 * with instrumentation behavior.
 *
 * <p>This is the public entry point to instrumentation.
 */
public final class Instrumentations {

  private Instrumentations() {}

  /**
   * Adds instrumentation support to an existent predictor builder, allowing it to create an
   * instrumented {@link com.spotify.zoltar.Predictor}.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      Function<
              PredictorBuilder<ModelT, InputT, VectorT, ValueT>,
              InstrumentedPredictorBuilder<ModelT, InputT, VectorT, ValueT>>
          predictor(final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return InstrumentedPredictorBuilder.create(metrics);
  }
}
