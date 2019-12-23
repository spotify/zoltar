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
package com.spotify.zoltar.examples.mlengine;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.tensorflow.example.Example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

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
import com.spotify.zoltar.mlengine.MlEnginePredictException;
import com.spotify.zoltar.mlengine.MlEnginePredictFn;

/** Cloud Machine Learning Engine online prediction example. */
public final class MlEnginePredictorExample implements Predictor<Iris, Integer> {

  private final Predictor<Iris, Integer> predictor;

  private MlEnginePredictorExample(final Predictor<Iris, Integer> predictor) {
    this.predictor = predictor;
  }

  /** Runs a simple Iris prediction against the running Iris ml-engine instance. */
  public static MlEnginePredictorExample create(
      final String projectId, final String modelId, final String versionId) throws Exception {
    // #MlEngineLoader
    final MlEngineLoader mlEngineLoader = Models.mlEngine(projectId, modelId, versionId);
    // #MlEngineLoader

    final FeatureSpec<Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    final URI settingsUri = MlEnginePredictorExample.class.getResource("/settings.json").toURI();
    final String settings = new String(Files.readAllBytes(Paths.get(settingsUri)));
    final ExtractFn<Iris, Example> extractFn = FeatranExtractFns.example(irisFeatureSpec, settings);

    final MlEnginePredictFn<Iris, Example, Integer> predictFn =
        (model, vectors) -> {
          final List<CompletableFuture<Prediction<Iris, Integer>>> predictions =
              vectors
                  .stream()
                  .map(
                      vector ->
                          CompletableFuture.supplyAsync(
                                  () -> {
                                    try {
                                      final List<Example> input =
                                          Collections.singletonList(vector.value());
                                      final MlEnginePrediction result =
                                          model
                                              .predictExamples(input)
                                              .values(MlEnginePrediction.class)
                                              .get(0);

                                      final int max =
                                          IntStream.range(0, result.scores().size())
                                              .reduce(
                                                  (i, j) -> {
                                                    final BigDecimal ci = result.scores().get(i);
                                                    final BigDecimal cj = result.scores().get(j);
                                                    return ci.compareTo(cj) > 0 ? i : j;
                                                  })
                                              .getAsInt();

                                      return result.classes().get(max);
                                    } catch (IOException
                                        | MlEnginePredictException
                                        | ExecutionException e) {
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
      final ScheduledExecutorService scheduler, final Duration timeout, final Iris... input) {
    return predictor.predict(scheduler, timeout, input);
  }

  @AutoValue
  abstract static class MlEnginePrediction {

    public abstract List<Integer> classes();

    public abstract List<BigDecimal> scores();

    @JsonCreator
    static MlEnginePrediction create(
        @JsonProperty("classes") final List<Integer> classes,
        @JsonProperty("scores") final List<BigDecimal> scores) {
      return new AutoValue_MlEnginePredictorExample_MlEnginePrediction(classes, scores);
    }
  }
}
