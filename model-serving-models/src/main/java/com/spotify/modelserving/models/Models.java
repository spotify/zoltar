/*-
 * -\-\-
 * model-serving-models
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

package com.spotify.modelserving.models;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.tf.TensorFlowGraphModel;
import com.spotify.modelserving.tf.TensorFlowModel;
import com.spotify.modelserving.xgboost.XGBoostModel;
import java.io.IOException;
import java.net.URI;
import javax.annotation.Nullable;
import org.tensorflow.framework.ConfigProto;

public final class Models {

  private Models() {
  }

  public static <T> XGBoostModel<T> xgboost(final String modelUri,
                                            final String settingsUri,
                                            final JFeatureSpec<T> featureSpec) throws IOException {
    return XGBoostModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  public static <T> XGBoostModel<T> xgboost(final String modelUri,
                                            final String settingsUri,
                                            final FeatureSpec<T> featureSpec) throws IOException {
    return XGBoostModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  public static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                                  final String settingsUri,
                                                  final FeatureSpec<T> featureSpec)
      throws IOException {
    return TensorFlowModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  public static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                                  final String settingsUri,
                                                  final JFeatureSpec<T> featureSpec)
      throws IOException {
    return TensorFlowModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  public static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                                  final String settingsUri,
                                                  final FeatureSpec<T> featureSpec,
                                                  final TensorFlowModel.Options options)
      throws IOException {
    return TensorFlowModel
        .create(URI.create(modelUri), URI.create(settingsUri), featureSpec, options);
  }

  public static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                                  final String settingsUri,
                                                  final JFeatureSpec<T> featureSpec,
                                                  final TensorFlowModel.Options options)
      throws IOException {
    return TensorFlowModel
        .create(URI.create(modelUri), URI.create(settingsUri), featureSpec, options);
  }

  public static <T> TensorFlowGraphModel<T> tensorFlowGraph(final String modelUri,
                                                            @Nullable final ConfigProto config,
                                                            @Nullable final String prefix,
                                                            final String settingsUri,
                                                            final JFeatureSpec<T> featureSpec)
      throws IOException {
    return TensorFlowGraphModel
        .from(URI.create(modelUri), config, prefix, URI.create(settingsUri), featureSpec);
  }

  public static <T> TensorFlowGraphModel<T> tensorFlowGraph(final String modelUri,
                                                            @Nullable final ConfigProto config,
                                                            @Nullable final String prefix,
                                                            final String settingsUri,
                                                            final FeatureSpec<T> featureSpec)
      throws IOException {
    return TensorFlowGraphModel.from(
        URI.create(modelUri),
        config,
        prefix,
        URI.create(settingsUri),
        JFeatureSpec.wrap(featureSpec));
  }
}
