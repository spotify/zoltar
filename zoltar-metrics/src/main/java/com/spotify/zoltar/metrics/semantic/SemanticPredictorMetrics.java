/*-
 * -\-\-
 * zoltar-metrics
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

package com.spotify.zoltar.metrics.semantic;

import static com.spotify.zoltar.metrics.semantic.What.FEATURE_EXTRACT_DURATION;
import static com.spotify.zoltar.metrics.semantic.What.FEATURE_EXTRACT_RATE;
import static com.spotify.zoltar.metrics.semantic.What.PREDICT_DURATION;
import static com.spotify.zoltar.metrics.semantic.What.PREDICT_RATE;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.auto.value.AutoValue;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Vector;
import com.spotify.zoltar.metrics.PredictorMetrics;
import java.util.List;
import java.util.concurrent.CompletionStage;

@AutoValue
public abstract class SemanticPredictorMetrics implements PredictorMetrics {

  abstract Timer predictTimer();

  abstract Meter predictMeter();

  abstract Timer extractTimer();

  abstract Meter extractMeter();

  /** Creates a new @{link SemanticPredictorMetrics}. */
  public static SemanticPredictorMetrics create(final SemanticMetricRegistry registry) {
    final MetricId predictDuration = MetricId.build().tagged("what", PREDICT_DURATION.tag());
    final MetricId predictRate = MetricId.build().tagged("what", PREDICT_RATE.tag());
    final MetricId featureExtractDuration =
        MetricId.build().tagged("what", FEATURE_EXTRACT_DURATION.tag());
    final MetricId featureExtractRate =
        MetricId.build().tagged("what", FEATURE_EXTRACT_RATE.tag());

    final Timer predictTimer = registry.timer(predictDuration);
    final Timer extractTimer = registry.timer(featureExtractDuration);
    final Meter predictMeter = registry.meter(predictRate);
    final Meter extractMeter = registry.meter(featureExtractRate);

    return new AutoValue_SemanticPredictorMetrics(predictTimer,
                                                  predictMeter,
                                                  extractTimer,
                                                  extractMeter);
  }

  @Override
  @SuppressWarnings("checkstyle:LineLength")
  public <ModelT extends Model<?>, InputT, VectorT, ValueT> AsyncPredictFn<ModelT, InputT, VectorT, ValueT> duration(
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> fn) {
    return (model, vectors) -> {
      final Context context = predictTimer().time();
      final CompletionStage<List<Prediction<InputT, ValueT>>> result = fn.apply(model, vectors);
      result.whenComplete((r, t) -> context.stop());

      return result;
    };
  }

  @Override
  public <InputT, VectorT> FeatureExtractor<InputT, VectorT> duration(
      final FeatureExtractor<InputT, VectorT> fn) {
    return inputs -> {
      final Context context = extractTimer().time();
      final List<Vector<InputT, VectorT>> result = fn.extract(inputs);
      context.stop();

      return result;
    };
  }

  @Override
  @SuppressWarnings("checkstyle:LineLength")
  public <ModelT extends Model<?>, InputT, VectorT, ValueT> AsyncPredictFn<ModelT, InputT, VectorT, ValueT> rate(
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> fn) {
    return (model, vectors) -> {
      final CompletionStage<List<Prediction<InputT, ValueT>>> result = fn.apply(model, vectors);
      result.whenComplete((r, t) -> predictMeter().mark());

      return result;
    };
  }

  @Override
  public <InputT, VectorT> FeatureExtractor<InputT, VectorT> rate(
      final FeatureExtractor<InputT, VectorT> fn) {
    return inputs -> {
      final List<Vector<InputT, VectorT>> result = fn.extract(inputs);
      extractMeter().mark();

      return result;
    };
  }

}
