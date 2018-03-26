/*-
 * -\-\-
 * zoltar-core
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

package com.spotify.zoltar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class PredictorTest {

  static class DummyModel implements Model<Object> {

    @Override
    public Object instance() {
      return new Object();
    }

    @Override
    public void close() throws Exception {

    }
  }

  @Test
  public void timeout() {
    final Duration wait = Duration.ofSeconds(1);
    final Duration predictionTimeout = Duration.ZERO;

    final ExtractFn<Object, Object> extractFn = inputs -> Collections.emptyList();
    final PredictFns.PredictFn<DummyModel, Object, Object, Object> predictFn =
        (model, vectors) -> null;

    try {
      Predictor.create(new DummyModel(), extractFn, predictFn)
          .predict(predictionTimeout, new Object())
          .toCompletableFuture()
          .get(wait.toMillis(), TimeUnit.MILLISECONDS);

      fail("should throw TimeoutException");
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof TimeoutException);
    }
  }
}
