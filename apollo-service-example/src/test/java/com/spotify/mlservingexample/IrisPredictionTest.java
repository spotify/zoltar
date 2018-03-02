/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.mlservingexample;

import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.Predictor;
import com.spotify.modelserving.tf.TensorFlowModel;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IrisPredictionTest {

  @Before
  public void loadModelAndPredictor(){

    final String path = "/Users/krishnasuray/Documents/work/final/model-serving/apollo-service-example/src/main/resources/trained_model";
    try {
      TensorFlowModel<Iris> model = IrisModel.loadModel(path);
      Predictor<Iris, Long> predictor = IrisPredictor.loadPredictor(model);
      IrisPrediction.setPredictor(predictor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPrediction(){

    final String testData = "5.3-2.7-2.0-1.9";
    final String expectedClass = "Iris-versicolor";
    final String predictedClass = IrisPrediction.predict(testData).payload().get();
    Assert.assertEquals(expectedClass,predictedClass);
  }
}
