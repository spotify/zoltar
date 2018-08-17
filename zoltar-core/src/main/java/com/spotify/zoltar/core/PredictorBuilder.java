/*-
 * -\-\-
 * zoltar-core
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

package com.spotify.zoltar.core;

import com.spotify.zoltar.core.PredictFns.AsyncPredictFn;
import java.util.function.Function;

/**
 * PredictorBuilder holds the necessary info to build a {@link Predictor}.
 *
 * @param <ModelT>  underlying type of the {@link Model}.
 * @param <InputT>  type of the input to the {@link FeatureExtractor}.
 * @param <VectorT> type of the output from {@link FeatureExtractor}.
 * @param <ValueT>  type of the prediction result.
 */
public interface PredictorBuilder<ModelT extends Model<?>, InputT, VectorT, ValueT> {

  ModelLoader<ModelT> modelLoader();

  FeatureExtractor<ModelT, InputT, VectorT> featureExtractor();

  AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn();

  Predictor<InputT, ValueT> predictor();

  PredictorBuilder<ModelT, InputT, VectorT, ValueT> with(
      ModelLoader<ModelT> modelLoader,
      FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
      AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn);

  @SuppressWarnings("unchecked")
  default <C extends PredictorBuilder<ModelT, InputT, VectorT, ValueT>> C with(
      final ModelLoader<ModelT> modelLoader) {
    return (C) with(modelLoader, featureExtractor(), predictFn());
  }

  @SuppressWarnings("unchecked")
  default <C extends PredictorBuilder<ModelT, InputT, VectorT, ValueT>> C with(
      final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor) {
    return (C) with(modelLoader(), featureExtractor, predictFn());
  }

  @SuppressWarnings("unchecked")
  default <C extends PredictorBuilder<ModelT, InputT, VectorT, ValueT>> C with(
      final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return (C) with(modelLoader(), featureExtractor(), predictFn);
  }

  default <C extends PredictorBuilder<ModelT, InputT, VectorT, ValueT>> C with(
      final Function<PredictorBuilder<ModelT, InputT, VectorT, ValueT>, C> fn) {
    return fn.apply(this);
  }

}
