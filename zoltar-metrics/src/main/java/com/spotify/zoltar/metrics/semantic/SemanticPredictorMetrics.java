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
import com.google.auto.value.AutoValue;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.zoltar.metrics.FeatureExtractorMetrics;
import com.spotify.zoltar.metrics.PredictFnMetrics;
import com.spotify.zoltar.metrics.PredictorMetrics;

/**
 * Semantic metric implementation for the {@link PredictorMetrics}.
 */
@AutoValue
public abstract class SemanticPredictorMetrics implements PredictorMetrics {

  abstract Timer predictDurationTimer();

  abstract Meter predictRateCounter();

  abstract Timer extractDurationTimer();

  abstract Meter extractRateCounter();

  /** Creates a new @{link SemanticPredictorMetrics}. */
  public static SemanticPredictorMetrics create(final SemanticMetricRegistry registry) {
    final MetricId predictDurationId = MetricId.build().tagged("what", PREDICT_DURATION.tag());
    final MetricId predictRateId = MetricId.build().tagged("what", PREDICT_RATE.tag());
    final MetricId extractDurationId = MetricId.build()
        .tagged("what", FEATURE_EXTRACT_DURATION.tag());
    final MetricId extractRateId = MetricId.build().tagged("what", FEATURE_EXTRACT_RATE.tag());

    final Timer predictDurationTimer = registry.timer(predictDurationId);
    final Meter predictRateCounter = registry.meter(predictRateId);
    final Timer extractDurationTimer = registry.timer(extractDurationId);
    final Meter extractRateCounter = registry.meter(extractRateId);

    return new AutoValue_SemanticPredictorMetrics(predictDurationTimer,
                                                  predictRateCounter,
                                                  extractDurationTimer,
                                                  extractRateCounter);
  }

  @Override
  public PredictFnMetrics predictFnMetrics() {
    return () -> SemanticPredictMetrics.create(predictDurationTimer().time(), predictRateCounter());
  }

  @Override
  public FeatureExtractorMetrics featureExtractorMetrics() {
    return () -> SemanticVectorMetrics.create(extractDurationTimer().time(), extractRateCounter());
  }
}
