/*-
 * -\-\-
 * apollo-service-example
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

package com.spotify.zoltar.examples.apollo;

import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.metrics.semantic.SemanticPredictorMetrics;
import java.io.IOException;
import java.net.URI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IrisPredictionTest {

  @Before
  public void loadModelAndPredictor() {
    try {
      final URI trainedModelUri = getClass().getResource("/trained_model").toURI();
      final URI settingsUri = getClass().getResource("/settings.json").toURI();
      final SemanticMetricRegistry semanticMetricRegistry = new SemanticMetricRegistry();
      IrisPrediction.configure(
          trainedModelUri,
          settingsUri,
          SemanticPredictorMetrics.create(semanticMetricRegistry));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPrediction() throws IOException {
    final String testData = "5.3-2.7-2.0-1.9";
    final String expectedClass = "Iris-versicolor";
    final String predictedClass = IrisPrediction.predict(testData).payload().get();
    Assert.assertEquals(expectedClass, predictedClass);
  }
}
