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
package com.spotify.zoltar.examples.batch;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn.UnaryExtractFn;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;

/** Example showing a batch predictor. */
class BatchPredictorExample implements Predictor<Integer, Float> {

  private Predictor<DummyModel, List<Integer>, List<Float>, List<Float>> predictor;

  BatchPredictorExample() {
    final ModelLoader<DummyModel> modelLoader = ModelLoader.loaded(new DummyModel());

    final ExtractFn<Integer, Float> batchExtractFn =
        ExtractFn.extract((UnaryExtractFn<Integer, Float>) input -> (float) input / 10);

    final PredictFn<DummyModel, Integer, Float, Float> predictFn =
        (model, vectors) -> {
          return vectors
              .stream()
              .map(
                  vector -> {
                    return Prediction.create(vector.input(), vector.value() * 2);
                  })
              .collect(Collectors.toList());
        };

    // We build the Predictor as usual
    predictor = Predictors.create(modelLoader, batchExtractFn, predictFn);
  }

  @Override
  public CompletionStage<List<Prediction<Integer, Float>>> predict(
      final ScheduledExecutorService scheduler, final Duration timeout, final List<Integer> input) {
    return predictorBuilder.predictor().predict(scheduler, timeout, input);
  }
}
