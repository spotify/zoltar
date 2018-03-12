/*-
 * -\-\-
 * zoltar-models
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

  public static XGBoostModel xgboost(final String modelUri) throws IOException {
    return XGBoostModel.create(URI.create(modelUri));
  }

  public static TensorFlowModel tensorFlow(final String modelUri) throws IOException {
    return TensorFlowModel.create(URI.create(modelUri));
  }

  public static TensorFlowModel tensorFlow(final String modelUri,
                                           final TensorFlowModel.Options options)
      throws IOException {
    return TensorFlowModel.create(URI.create(modelUri), options);
  }

  public static TensorFlowGraphModel tensorFlowGraph(final String modelUri,
                                                     @Nullable final ConfigProto config,
                                                     @Nullable final String prefix)
      throws IOException {
    return TensorFlowGraphModel.from(URI.create(modelUri), config, prefix);
  }
}
