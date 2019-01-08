/*
 * Copyright (C) 2016 - 2018 Spotify AB
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
package com.spotify.zoltar.examples.metrics;

import org.junit.Assert;
import org.junit.Test;

import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

public class CustomMetricsExampleTest {

  @Test
  public void testCustomMetrics() throws Exception {
    // #SemanticMetricRegistry
    final SemanticMetricRegistry registry = new SemanticMetricRegistry();
    final MetricId metricId = MetricId.build().tagged("service", "my-application");
    // #SemanticMetricRegistry

    final CustomMetricsExample example = new CustomMetricsExample(registry, metricId);

    example.predict(3, 1, -4, -42, 42, -10).toCompletableFuture().join();

    registry.getCounters().values().forEach(counter -> Assert.assertEquals(3, counter.getCount()));
  }
}
