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
package com.spotify.zoltar.mlengine;

import com.spotify.zoltar.PredictFns.AsyncPredictFn;

/**
 * Google Cloud ML Engine flavor of {@link AsyncPredictFn} using {@link MlEngineModel}.
 *
 * @param <InputT> type of the raw input to the feature extraction.
 * @param <VectorT> type of the feature extraction output.
 * @param <ValueT> type of the prediction result.
 */
@FunctionalInterface
public interface MlEnginePredictFn<InputT, VectorT, ValueT>
    extends AsyncPredictFn<MlEngineModel, InputT, VectorT, ValueT> {}
