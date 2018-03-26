/*-
 * -\-\-
 * zoltar-xgboost
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

package com.spotify.zoltar.xgboost;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.IrisHelper;
import com.spotify.zoltar.loaders.Memoizer;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.featran.FeatranExtractFns;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ml.dmlc.xgboost4j.LabeledPoint;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.Test;

public class XGBoostModelTest {

  @Test
  public void testLoadingModel() throws Exception {
    final URI trainedModel = getClass().getResource("/iris.model").toURI();
    XGBoostModel.create(trainedModel);
  }

  public static Predictor<Iris, Long> getXGBoostIrisPredictor() throws Exception {
    final URI trainedModelUri = XGBoostModelTest.class.getResource("/iris.model").toURI();
    final URI settingsUri = XGBoostModelTest.class.getResource("/settings.json").toURI();

    final XGBoostPredictFn<Iris, Long> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Long>>> predictions =
          vectors.stream().map(vector -> {
            return CompletableFuture.supplyAsync(() -> {
              try {
                final Iterator<LabeledPoint> iterator =
                    Collections.singletonList(vector.value()).iterator();
                final DMatrix dMatrix = new DMatrix(iterator, null);
                final float[] score = model.instance().predict(dMatrix)[0];
                int idx = IntStream.range(0, score.length)
                    .reduce((i, j) -> score[i] >= score[j] ? i : j)
                    .getAsInt();
                return Prediction.create(vector.input(), (long)idx);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
          }).collect(Collectors.toList());

      return CompletableFutures.allAsList(predictions);
    };

    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)),
        StandardCharsets.UTF_8);
    final ModelLoader<XGBoostModel> model = Memoizer.memoize(() -> {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return XGBoostModel.create(trainedModelUri);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      });
    });

    // preload
    model.get();

    final ExtractFn<Iris, LabeledPoint> extractFn = FeatranExtractFns
        .labeledPoints(IrisFeaturesSpec.irisFeaturesSpec(), settings);

    return Predictor.create(model, extractFn, predictFn);
  }

  @Test
  public void testModelPrediction() throws Exception {
    final Iris[] irisStream = IrisHelper.getIrisTestData();

    final Map<Integer, String> classToId = ImmutableMap.of(0, "Iris-setosa",
        1, "Iris-versicolor",
        2, "Iris-virginica");

    final CompletableFuture<Integer> sum = getXGBoostIrisPredictor()
        .predict(Duration.ofMillis(1000), irisStream)
        .thenApply(predictions -> {
          return predictions.stream()
              .mapToInt(prediction -> {
                String className = prediction.input().className().get();
                int score = prediction.value().intValue();
                return classToId.get(score).equals(className) ? 1 : 0;
              }).sum();
        }).toCompletableFuture();

    assertTrue("Should be more the 0.8", sum.get() / (float) irisStream.length > .8);
  }
}
