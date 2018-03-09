/*-
 * -\-\-
 * model-serving-tensorflow
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

package com.spotify.modelserving.tf;

import com.google.auto.value.AutoValue;
import com.spotify.modelserving.Model;
import com.spotify.modelserving.fs.FileSystemExtras;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.tensorflow.SavedModelBundle;

public class TensorFlowModel implements Model<SavedModelBundle> {

  private static final Options DEFAULT_OPTIONS = Options.builder()
      .tags(Collections.singletonList("serve"))
      .build();

  private final SavedModelBundle model;
  private final Options options;

  public static TensorFlowModel create(String modelUri) throws IOException {
    return create(modelUri, DEFAULT_OPTIONS);
  }

  public static TensorFlowModel create(String modelUri,
                                       Options options) throws IOException {
    return create(URI.create(modelUri), options);
  }

  public static TensorFlowModel create(URI modelResource) throws IOException {
    return create(modelResource, DEFAULT_OPTIONS);
  }

  public static TensorFlowModel create(URI modelResource,
                                       Options options) throws IOException {
    return new TensorFlowModel(modelResource.toString(), options);
  }

  private TensorFlowModel(String exportDir, Options options) throws IOException {
    final URI localDir = FileSystemExtras.downloadIfNonLocal(URI.create(exportDir));
    this.model = SavedModelBundle.load(localDir.toString(), options.tags().toArray(new String[0]));
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
