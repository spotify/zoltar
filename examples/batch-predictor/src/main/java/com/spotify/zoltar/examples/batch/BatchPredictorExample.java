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

import com.spotify.zoltar.FeatureExtractFns.BatchExtractFn;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.PredictorBuilder;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.loaders.Preloader;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Example showing a batch predictor.
 */
class BatchPredictorExample implements Predictor<List<Integer>, List<Float>> {

  @SuppressWarnings("checkstyle:LineLength")
  private final PredictorBuilder<DummyModel, List<Integer>, List<Float>, List<Float>> predictorBuilder;

  BatchPredictorExample() {
    final ModelLoader<DummyModel> modelLoader = ModelLoader
        .lift(DummyModel::new)
        .with(Preloader.preload(Duration.ofMinutes(1)));

    final BatchExtractFn<Integer, Float> batchExtractFn =
        BatchExtractFn.lift((Function<Integer, Float>) input -> (float) input / 10);

    final PredictFn<DummyModel, List<Integer>, List<Float>, List<Float>> predictFn =
        (model, vectors) -> {
          return vectors.stream()
              .map(vector -> {
                final List<Float> values = vector.value().stream().map(v -> v * 2)
                    .collect(Collectors.toList());

                return Prediction.create(vector.input(), values);
              })
              .collect(Collectors.toList());
        };

    // We build the PredictorBuilder as usual
    predictorBuilder = Predictors.newBuilder(modelLoader, batchExtractFn, predictFn);
  }

  @Override
  public CompletionStage<List<Prediction<List<Integer>, List<Float>>>> predict(
      final ScheduledExecutorService scheduler,
      final Duration timeout,
      final List<Integer>... input) {
    return predictorBuilder.predictor().predict(scheduler, timeout, input);
  }

}
