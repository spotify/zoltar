/*
 * Copyright (C) 2016 - 2018 Spotify AB
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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.spotify.zoltar.FeatureExtractFns.BatchExtractFn;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;

/** Example showing a batch predictor. */
class BatchPredictorExample
    implements Predictor<DummyModel, List<Integer>, List<Float>, List<Float>> {

  private Predictor<DummyModel, List<Integer>, List<Float>, List<Float>> predictor;

  BatchPredictorExample() {
    final ModelLoader<DummyModel> modelLoader = ModelLoader.loaded(new DummyModel());

    final BatchExtractFn<Integer, Float> batchExtractFn =
        BatchExtractFn.lift((Function<Integer, Float>) input -> (float) input / 10);

    final PredictFn<DummyModel, List<Integer>, List<Float>, List<Float>> predictFn =
        (model, vectors) -> {
          return vectors
              .stream()
              .map(
                  vector -> {
                    final List<Float> values =
                        vector.value().stream().map(v -> v * 2).collect(Collectors.toList());

                    return Prediction.create(vector.input(), values);
                  })
              .collect(Collectors.toList());
        };

    // We build the Predictor as usual
    predictor = Predictors.create(modelLoader, batchExtractFn, predictFn);
  }

  @Override
  public ModelLoader<DummyModel> modelLoader() {
    return predictor.modelLoader();
  }

  @Override
  public FeatureExtractor<DummyModel, List<Integer>, List<Float>> featureExtractor() {
    return predictor.featureExtractor();
  }

  @Override
  public AsyncPredictFn<DummyModel, List<Integer>, List<Float>, List<Float>> predictFn() {
    return predictor.predictFn();
  }
}
