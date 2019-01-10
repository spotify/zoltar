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
package com.spotify.zoltar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;

public class PredictorBuilderTest {

  static class DummyModel implements Model<Object> {

    @Override
    public Id id() {
      return Id.create("dummy");
    }

    @Override
    public Object instance() {
      return new Object();
    }

    @Override
    public void close() throws Exception {}
  }

  static final class IdentityPredictor<ModelT extends Model<?>, InputT, VectorT, ValueT>
      implements Predictor<ModelT, InputT, VectorT, ValueT> {

    private final Predictor<ModelT, InputT, VectorT, ValueT> predictor;

    IdentityPredictor(final Predictor<ModelT, InputT, VectorT, ValueT> predictor) {
      this.predictor = predictor;
    }

    public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
        Function<
                Predictor<ModelT, InputT, VectorT, ValueT>,
                IdentityPredictor<ModelT, InputT, VectorT, ValueT>>
            decorate() {
      return IdentityPredictor::new;
    }

    @Override
    public ModelLoader<ModelT> modelLoader() {
      return predictor.modelLoader();
    }

    @Override
    public FeatureExtractor<ModelT, InputT, VectorT> featureExtractor() {
      return predictor.featureExtractor();
    }

    @Override
    public AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn() {
      return predictor.predictFn();
    }
  }

  @Test
  public void identityDecoration() throws ExecutionException, InterruptedException {
    final ModelLoader<DummyModel> loader = ModelLoader.loaded(new DummyModel());
    final ExtractFn<Integer, Float> extractFn = ExtractFn.lift(input -> (float) input / 10);
    final PredictFn<DummyModel, Integer, Float, Float> predictFn =
        (model, vectors) -> {
          return vectors
              .stream()
              .map(vector -> Prediction.create(vector.input(), vector.value() * 2))
              .collect(Collectors.toList());
        };

    final List<Prediction<Integer, Float>> predictions =
        Predictors.create(loader, extractFn, predictFn)
            .with(IdentityPredictor.decorate())
            .predict(1)
            .toCompletableFuture()
            .get();

    assertThat(predictions.size(), is(1));
    assertThat(predictions.get(0), is(Prediction.create(1, 0.2f)));
  }
}
