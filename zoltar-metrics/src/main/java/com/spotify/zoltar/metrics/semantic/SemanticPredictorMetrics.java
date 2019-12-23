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

import static com.spotify.zoltar.metrics.semantic.What.FEATURE_EXTRACT_DURATION;
import static com.spotify.zoltar.metrics.semantic.What.FEATURE_EXTRACT_RATE;
import static com.spotify.zoltar.metrics.semantic.What.PREDICT_DURATION;
import static com.spotify.zoltar.metrics.semantic.What.PREDICT_RATE;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.auto.value.AutoValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.zoltar.Model.Id;
import com.spotify.zoltar.metrics.FeatureExtractorMetrics;
import com.spotify.zoltar.metrics.PredictFnMetrics;
import com.spotify.zoltar.metrics.PredictorMetrics;

/** Semantic metric implementation for the {@link PredictorMetrics}. */
@AutoValue
public abstract class SemanticPredictorMetrics<InputT, VectorT, ValueT>
    implements PredictorMetrics<InputT, VectorT, ValueT> {

  abstract LoadingCache<Id, Metrics> metricsCache();

  /** Creates a new @{link SemanticPredictorMetrics}. */
  public static <InputT, VectorT, ValueT> SemanticPredictorMetrics<InputT, VectorT, ValueT> create(
      final SemanticMetricRegistry registry, final MetricId metricId) {
    final LoadingCache<Id, Metrics> metersCache =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<Id, Metrics>() {
                  @Override
                  public Metrics load(final Id id) {
                    return Metrics.create(registry, metricId.tagged("model", id.value()));
                  }
                });

    return new AutoValue_SemanticPredictorMetrics<>(metersCache);
  }

  @Override
  public PredictFnMetrics<InputT, ValueT> predictFnMetrics() {
    return id -> {
      final Metrics metrics = metricsCache().getUnchecked(id);
      final Context time = metrics.predictDurationTimer().time();
      final Meter meter = metrics.predictRateCounter();

      return SemanticPredictMetrics.create(time, meter);
    };
  }

  @Override
  public FeatureExtractorMetrics<InputT, VectorT> featureExtractorMetrics() {
    return id -> {
      final Metrics metrics = metricsCache().getUnchecked(id);
      final Context time = metrics.extractDurationTimer().time();
      final Meter meter = metrics.extractRateCounter();

      return SemanticVectorMetrics.create(time, meter);
    };
  }

  @AutoValue
  abstract static class Metrics {

    abstract Timer predictDurationTimer();

    abstract Meter predictRateCounter();

    abstract Timer extractDurationTimer();

    abstract Meter extractRateCounter();

    static Metrics create(final SemanticMetricRegistry registry, final MetricId metricId) {
      final MetricId predictDurationId = metricId.tagged("what", PREDICT_DURATION.tag());
      final MetricId predictRateId = metricId.tagged("what", PREDICT_RATE.tag());
      final MetricId extractDuration = metricId.tagged("what", FEATURE_EXTRACT_DURATION.tag());
      final MetricId extractRate = metricId.tagged("what", FEATURE_EXTRACT_RATE.tag());

      final Timer predictTimer = registry.timer(predictDurationId);
      final Meter predictMeter = registry.meter(predictRateId);
      final Timer extractTimer = registry.timer(extractDuration);
      final Meter extractMeter = registry.meter(extractRate);

      return new AutoValue_SemanticPredictorMetrics_Metrics(
          predictTimer, predictMeter, extractTimer, extractMeter);
    }
  }
}
