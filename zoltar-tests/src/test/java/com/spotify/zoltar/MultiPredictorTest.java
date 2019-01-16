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
package com.spotify.zoltar;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import ml.dmlc.xgboost4j.LabeledPoint;

import org.junit.Test;
import org.tensorflow.example.Example;

import com.google.common.collect.Lists;

import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.tf.TensorFlowModelTest;
import com.spotify.zoltar.xgboost.XGBoostModel;
import com.spotify.zoltar.xgboost.XGBoostModelTest;

public class MultiPredictorTest {

  @Test
  public void testMultiplePredictors() throws Exception {
    final Predictor<TensorFlowModel, Iris, Example, Long> tfPredictor =
        TensorFlowModelTest.getTFIrisPredictor();
    final Predictor<XGBoostModel, Iris, LabeledPoint, Long> xgBoostPredictor =
        XGBoostModelTest.getXGBoostIrisPredictor();
    final ArrayList<Predictor<?, IrisFeaturesSpec.Iris, ?, Long>> predictors =
        Lists.newArrayList(tfPredictor, xgBoostPredictor);
    final List<Iris> irisStream = IrisHelper.getIrisTestData();

    predictors
        .stream()
        .map(
            predictor -> {
              try {
                return predictor
                    .predict(irisStream)
                    .thenApply(
                        predictions ->
                            predictions
                                .stream()
                                .map(Prediction::value)
                                // count by value
                                .collect(
                                    Collectors.groupingBy(
                                        Function.identity(), Collectors.counting()))
                                .values()
                                .stream()
                                // devide each class count by number of elements - perfect predictor
                                // should
                                // have 0.(3) for each class
                                .map(classCount -> classCount / 150.0f)
                                .collect(Collectors.toList()))
                    .toCompletableFuture();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .flatMap(
            predictionFuture -> {
              try {
                return predictionFuture.get().stream();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        // probability of each class should be close to 0.(3)
        .forEach(f -> assertEquals(0.3f, f, 0.15f));
  }
}
