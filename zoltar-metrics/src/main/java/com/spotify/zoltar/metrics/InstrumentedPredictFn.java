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

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Prediction;

/**
 * Instrumented Asynchronous prediction functional interface {@link AsyncPredictFn}.
 *
 * @param <InputT> type of the feature extraction input.
 * @param <VectorT> type of the feature extraction output.
 * @param <ValueT> type of the prediction output.
 */
@FunctionalInterface
interface InstrumentedPredictFn<ModelT extends Model<?>, InputT, VectorT, ValueT>
    extends AsyncPredictFn<ModelT, InputT, VectorT, ValueT> {

  /** Creates a new instrumented {@link AsyncPredictFn}. */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      Function<
              AsyncPredictFn<ModelT, InputT, VectorT, ValueT>,
              InstrumentedPredictFn<ModelT, InputT, VectorT, ValueT>>
          create(final PredictFnMetrics<InputT, ValueT> metrics) {
    return predictfn ->
        (model, vectors) -> {
          final PredictMetrics<InputT, ValueT> predictMetrics = metrics.apply(model.id());
          final CompletionStage<List<Prediction<InputT, ValueT>>> result =
              predictfn.apply(model, vectors);

          return result.whenComplete((r, t) -> predictMetrics.prediction(r));
        };
  }
}
