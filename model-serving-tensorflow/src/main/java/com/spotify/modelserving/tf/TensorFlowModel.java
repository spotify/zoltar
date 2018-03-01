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

import com.google.auto.value.AutoValue;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.Model;
import com.spotify.modelserving.fs.FileSystems;
import com.spotify.modelserving.fs.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.tensorflow.SavedModelBundle;

public class TensorFlowModel<T> implements Model<SavedModelBundle, T> {

  private static final Options DEFAULT_OPTIONS = Options.builder()
      .tags(Collections.singletonList("serve"))
      .build();

  private final SavedModelBundle model;
  private final String settings;
  private final JFeatureSpec<T> featureSpec;
  private final Options options;

  public static <T> TensorFlowModel<T> create(String modelUri,
                                              String settingsUri,
                                              FeatureSpec<T> featureSpec) throws IOException {
    return create(modelUri, settingsUri, JFeatureSpec.wrap(featureSpec), DEFAULT_OPTIONS);
  }

  public static <T> TensorFlowModel<T> create(String modelUri,
                                              String settingsUri,
                                              FeatureSpec<T> featureSpec,
                                              Options options) throws IOException {
    return create(URI.create(modelUri),
                  URI.create(settingsUri),
                  JFeatureSpec.wrap(featureSpec),
                  options);
  }

  public static <T> TensorFlowModel<T> create(URI modelResource,
                                              URI settingsResource,
                                              FeatureSpec<T> featureSpec) throws IOException {
    return create(modelResource, settingsResource, JFeatureSpec.wrap(featureSpec), DEFAULT_OPTIONS);
  }

  public static <T> TensorFlowModel<T> create(URI modelResource,
                                              URI settingsResource,
                                              FeatureSpec<T> featureSpec,
                                              Options options) throws IOException {

    return create(modelResource, settingsResource, JFeatureSpec.wrap(featureSpec), options);
  }

  public static <T> TensorFlowModel<T> create(String modelUri,
                                              String settingsUri,
                                              JFeatureSpec<T> featureSpec) throws IOException {
    return create(modelUri, settingsUri, featureSpec, DEFAULT_OPTIONS);
  }

  public static <T> TensorFlowModel<T> create(String modelUri,
                                              String settingsUri,
                                              JFeatureSpec<T> featureSpec,
                                              Options options) throws IOException {
    return create(URI.create(modelUri), URI.create(settingsUri), featureSpec, options);
  }

  public static <T> TensorFlowModel<T> create(URI modelResource,
                                              URI settingsResource,
                                              JFeatureSpec<T> featureSpec) throws IOException {
    return create(modelResource, settingsResource, featureSpec, DEFAULT_OPTIONS);
  }

  public static <T> TensorFlowModel<T> create(URI modelResource,
                                              URI settingsResource,
                                              JFeatureSpec<T> featureSpec,
                                              Options options) throws IOException {
    final String settings = Resource.from(settingsResource).read(asString());
    return new TensorFlowModel<>(modelResource.toString(), settings, featureSpec, options);
  }

  private TensorFlowModel(String exportDir,
                          String settings,
                          JFeatureSpec<T> featureSpec,
                          Options options) {
    String localDir = FileSystems.downloadIfNonLocal(URI.create(exportDir));
    this.model = SavedModelBundle.load(localDir, options.tags().toArray(new String[0]));
    this.settings = settings;
    this.featureSpec = featureSpec;
    this.options = options;
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
  public JFeatureSpec<T> featureSpec() {
    return featureSpec;
  }

  public Options options() {
    return options;
  }

  @AutoValue
  public abstract static class Options {

    // tags come from: https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/saved_model/tag_constants.py#L26
    public abstract List<String> tags();

    public static Builder builder() {
      return new AutoValue_TensorFlowModel_Options.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder tags(List<String> tags);

      public abstract Options build();
    }
  }

}
