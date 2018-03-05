/*-
 * -\-\-
 * model-serving-xgboost
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

package com.spotify.modelserving.xgboost;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.modelserving.IrisFeaturesSpec;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.FeatureExtractFn;
import com.spotify.modelserving.Model.Prediction;
import com.spotify.modelserving.Model.Predictor;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ml.dmlc.xgboost4j.LabeledPoint;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.Test;
import scala.Option;

public class XGBoostModelTest {

  @Test
  public void testLoadingModel() throws Exception {
    final URI trainedModel = getClass().getResource("/iris.model").toURI();
    final URI settings = getClass().getResource("/settings.json").toURI();
    final FeatureSpec<Iris> featuresSpec = IrisFeaturesSpec.irisFeaturesSpec();

    XGBoostModel.create(trainedModel, settings, featuresSpec);
  }

  @Test
  public void testModelPrediction() throws Exception {
    final URI trainedModel = getClass().getResource("/iris.model").toURI();
    final URI settings = getClass().getResource("/settings.json").toURI();
    final URI data = getClass().getResource("/iris.csv").toURI();

    final List<Iris> irisStream = Files.readAllLines(Paths.get(data))
        .stream()
        .map(l -> l.split(","))
        .map(strs -> new Iris(Option.apply(Double.parseDouble(strs[0])),
                              Option.apply(Double.parseDouble(strs[1])),
                              Option.apply(Double.parseDouble(strs[2])),
                              Option.apply(Double.parseDouble(strs[3])),
                              Option.apply(strs[4])))
        .collect(Collectors.toList());

    final Map<Integer, String> classToId = ImmutableMap.of(0, "Iris-setosa",
                                                           1, "Iris-versicolor",
                                                           2, "Iris-virginica");

    final XGBoostPredictFn<Iris, float[]> predictFn = (model, vectors) -> {
      return vectors.stream().map(vector -> {
        LabeledPoint labeledPoints = new LabeledPoint(0, null, vector.value());
        try {
          final Iterator<LabeledPoint> iterator =
              Collections.singletonList(labeledPoints).iterator();
          final DMatrix dMatrix = new DMatrix(iterator, null);

          return Prediction.create(vector.input(), model.instance().predict(dMatrix)[0]);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList());
    };

    final XGBoostModel<Iris> model = XGBoostModel.create(trainedModel,
                                                         settings,
                                                         IrisFeaturesSpec.irisFeaturesSpec());
    final FeatureExtractFn<Iris, float[]> featureExtractFn = JFeatureExtractor::featureValuesFloat;

    final IntStream predictions = Predictor
        .create(model, featureExtractFn, predictFn)
        .predict(irisStream)
        .stream()
        .mapToInt(prediction -> {
          String className = prediction.input().className().get();
          float[] score = prediction.value();
          int idx = IntStream.range(0, score.length)
              .reduce((i, j) -> score[i] >= score[j] ? i : j)
              .getAsInt();

          return classToId.get(idx).equals(className) ? 1 : 0;
        });

    assertTrue("Should be more the 0.8", predictions.sum() / (float) irisStream.size() > .8);
  }
}