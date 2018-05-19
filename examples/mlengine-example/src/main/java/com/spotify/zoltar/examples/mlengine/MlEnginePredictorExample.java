/*-
 * -\-\-
 * mlengine-example
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

/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spotify.zoltar.examples.mlengine;

import com.spotify.featran.FeatureSpec;
import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Models;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.mlengine.MlEngineLoader;
import com.spotify.zoltar.mlengine.MlEnginePredictFn;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.tensorflow.example.Example;

/**
 * Cloud Machine Learning Engine online prediction example.
 */
public final class MlEnginePredictorExample implements Predictor<Iris, Integer> {

  private final Predictor<Iris, Integer> predictor;

  private MlEnginePredictorExample(final Predictor<Iris, Integer> predictor) {
    this.predictor = predictor;
  }

  /**
   * Runs a simple Iris prediction against the running Iris ml-engine instance.
   */
  public static MlEnginePredictorExample create(final String projectId,
                                                final String modelId,
                                                final String versionId) throws Exception {
    final MlEngineLoader mlEngineLoader = Models.mlEngine(projectId, modelId, versionId);

    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final URI settingsUri = MlEnginePredictorExample.class.getResource("/settings.json").toURI();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)));
    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(irisFeatureSpec, settings);

    final MlEnginePredictFn<Iris, Example, Integer> predictFn = (model, vectors) -> {
      final List<CompletableFuture<Prediction<Iris, Integer>>> predictions =
          vectors.stream()
              .map(vector -> CompletableFuture
                  .supplyAsync(() -> {
                    try {
                      final List<Example> input = Collections.singletonList(vector.value());
                      final List<Map<String, List<?>>> result = model.predictExamples(input);

                      final List<BigDecimal> scores =
                          (List<BigDecimal>) result.get(0).get("scores");

                      final int max = IntStream.range(0, scores.size())
                          .reduce((i, j) -> scores.get(i).compareTo(scores.get(j)) > 0 ? i : j)
                          .getAsInt();

                      return new Integer((String) result.get(0).get("classes").get(max));
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  })
                  .thenApply(v -> Prediction.create(vector.input(), v)))
              .collect(Collectors.toList());
      return CompletableFutures.allAsList(predictions);
    };

    final Predictor<Iris, Integer> predictor =
        Predictors.newBuilder(mlEngineLoader, extractFn, predictFn).predictor();

    return new MlEnginePredictorExample(predictor);
  }

  @Override
  public CompletionStage<List<Prediction<Iris, Integer>>> predict(
      final ScheduledExecutorService scheduler,
      final Duration timeout,
      final Iris... input) {
    return predictor.predict(scheduler, timeout, input);
  }
}

