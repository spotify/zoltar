/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving.tf;

import static com.spotify.modelserving.fs.Resource.ReadFns.asString;

import com.spotify.featran.FeatureSpec;
import com.spotify.modelserving.Model;
import com.spotify.modelserving.fs.Resource;
import java.io.IOException;
import java.net.URI;
import org.tensorflow.SavedModelBundle;

public class TensorFlowModel<T> implements Model<SavedModelBundle, T> {

  private final SavedModelBundle model;
  private final String settings;
  private final FeatureSpec<T> featureSpec;

  public static <T> TensorFlowModel<T> create(URI modelResource,
                                              URI settingsResource,
                                              FeatureSpec<T> featureSpec) throws IOException {
    final String settings = Resource.from(settingsResource).read(asString());
    return new TensorFlowModel<>(modelResource.toString(), settings, featureSpec);
  }

  private TensorFlowModel(String exportDir,
                          String settings,
                          FeatureSpec<T> featureSpec) {
    // TODO: copy saved model from remote FS, object stores etc to local filesystem
    // tags come from: https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/saved_model/tag_constants.py#L26
    this.model = SavedModelBundle.load(exportDir, "serve");
    this.settings = settings;
    this.featureSpec = featureSpec;
  }

  @Override
  public void close() throws Exception {
    if (model != null) {
      model.close();
    }
  }

  @Override
  public SavedModelBundle instance() {
    return model;
  }

  @Override
  public String settings() {
    return settings;
  }

  @Override
  public FeatureSpec<T> featureSpec() {
    return featureSpec;
  }

}
