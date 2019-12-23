/*-
 * -\-\-
 * custom-metrics
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

package com.spotify.zoltar.examples.batch;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.spotify.zoltar.Prediction;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BatchPredictorExampleTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testCustomMetrics() {
    final BatchPredictorExample example = new BatchPredictorExample();

    final List<Integer> batch = ImmutableList.of(3, 1, -4, -42, 42, -10);
    final List<Float> predictions = example.predict(batch)
        .toCompletableFuture()
        .join()
        .stream()
        .map(Prediction::value)
        .findFirst()
        .orElse(Collections.emptyList());

    final List<Float> expected = batch.stream()
        .map(v -> (float) v / 10 * 2)
        .collect(Collectors.toList());

    assertThat(predictions, is(expected));
  }
}
