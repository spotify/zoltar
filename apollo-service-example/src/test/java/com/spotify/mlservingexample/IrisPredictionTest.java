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

package com.spotify.mlservingexample;

import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.Predictor;
import com.spotify.modelserving.tf.TensorFlowModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IrisPredictionTest {

  @Before
  public void loadModelAndPredictor() {
    try {
      Path trainedModelTempDir = Files.createTempDirectory("trained_model");

      URL savedModelURL = this.getClass().getResource("/trained_model/saved_model.pb");
      File savedModelFile = trainedModelTempDir.resolve("saved_model.pb").toFile();
      FileUtils.copyURLToFile(savedModelURL, savedModelFile);

      URL variablesDataUrl =
          this.getClass().getResource("/trained_model/variables/variables.data-00000-of-00001");
      File variableDataFile = trainedModelTempDir
          .resolve("variables")
          .resolve("variables.data-00000-of-00001").toFile();
      FileUtils.copyURLToFile(variablesDataUrl, variableDataFile);

      URL variablesIndexUrl =
          this.getClass().getResource("/trained_model/variables/variables.index");
      File variablesIndexFile = trainedModelTempDir
          .resolve("variables")
          .resolve("variables.index").toFile();
      FileUtils.copyURLToFile(variablesIndexUrl, variablesIndexFile);

      URL settingsUrl =
          this.getClass().getResource("/settings.json");
      File settingsFile = trainedModelTempDir
          .resolve("settings.json").toFile();
      FileUtils.copyURLToFile(settingsUrl, settingsFile);

      String path = trainedModelTempDir.toUri().toString();

      TensorFlowModel<Iris> model = IrisModel.loadModel(
          path,
          trainedModelTempDir.resolve("settings.json").toUri().toString());
      Predictor<Iris, Long> predictor = IrisPredictor.loadPredictor(model);
      IrisPrediction.setPredictor(predictor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPrediction() {
    final String testData = "5.3-2.7-2.0-1.9";
    final String expectedClass = "Iris-versicolor";
    final String predictedClass = IrisPrediction.predict(testData).payload().get();
    Assert.assertEquals(expectedClass, predictedClass);
  }
}
