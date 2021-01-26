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
package com.spotify.zoltar.tf;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.proto.framework.MetaGraphDef;
import org.tensorflow.proto.framework.SignatureDef;
import org.tensorflow.proto.framework.TensorInfo;

import com.google.auto.value.AutoValue;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystem;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.fs.FileSystemExtras;

/**
 * This model can be used to load TensorFlow {@link SavedModelBundle} model. Whenever possible
 * {@link TensorFlowModel} should be used in favour of {@link TensorFlowGraphModel}.
 *
 * <p>TensorFlowModel is thread-safe.
 */
@AutoValue
public abstract class TensorFlowModel implements Model<SavedModelBundle> {

  private static final Model.Id DEFAULT_ID = Id.create("tensorflow");
  private static final Options DEFAULT_OPTIONS =
      Options.builder().tags(Collections.singletonList("serve")).build();
  private static final String DEFAULT_SIGNATURE_DEF = "serving_default";

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * <p>Returns a TensorFlow model given {@link SavedModelBundle} export directory URI.
   */
  public static TensorFlowModel create(final URI modelResource) throws IOException {
    return create(DEFAULT_ID, modelResource, DEFAULT_OPTIONS);
  }

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * <p>Returns a TensorFlow model given {@link SavedModelBundle} export directory URI.
   */
  public static TensorFlowModel create(final Model.Id id, final URI modelResource)
      throws IOException {
    return create(id, modelResource, DEFAULT_OPTIONS);
  }

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * <p>Returns a TensorFlow model given {@link SavedModelBundle} export directory URI and {@link
   * Options}.
   */
  public static TensorFlowModel create(final URI modelResource, final Options options)
      throws IOException {
    return create(DEFAULT_ID, modelResource, options);
  }

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * <p>Returns a TensorFlow model with metadata given {@link SavedModelBundle} export directory URI
   * and {@link Options}.
   */
  public static TensorFlowModel create(
      final Model.Id id, final URI modelResource, final Options options) throws IOException {
    return create(id, modelResource, options, DEFAULT_SIGNATURE_DEF);
  }

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * <p>Returns a TensorFlow model with metadata given {@link SavedModelBundle} export directory URI
   * and {@link Options}.
   */
  public static TensorFlowModel create(
      final Model.Id id,
      final URI modelResource,
      final Options options,
      final String signatureDefinition)
      throws IOException {
    // GCS requires that directory URIs have a trailing slash, so add the slash if
    // it's missing
    // and the URI starts with 'gs'.
    final URI normalizedUri =
        !CloudStorageFileSystem.URI_SCHEME.equalsIgnoreCase(modelResource.getScheme())
                || modelResource.toString().endsWith("/")
            ? modelResource
            : URI.create(modelResource.toString() + "/");
    final URI localDir = FileSystemExtras.downloadIfNonLocal(normalizedUri);
    final SavedModelBundle model =
        SavedModelBundle.load(localDir.toString(), options.tags().toArray(new String[0]));
    final SignatureDef signatureDef =
        model.metaGraphDef().getSignatureDefOrThrow(signatureDefinition);

    return new AutoValue_TensorFlowModel(
        id,
        model,
        options,
        model.metaGraphDef(),
        signatureDef,
        toNameMap(signatureDef.getInputsMap()),
        toNameMap(signatureDef.getOutputsMap()));
  }

  /** Close the model. */
  @Override
  public void close() {
    if (instance() != null) {
      instance().close();
    }
  }

  /** Returns TensorFlow {@link SavedModelBundle}. */
  @Override
  public abstract SavedModelBundle instance();

  /** {@link Options} of this model. */
  public abstract Options options();

  public abstract MetaGraphDef metaGraphDefinition();

  public abstract SignatureDef signatureDefinition();

  public abstract Map<String, String> inputsNameMap();

  public abstract Map<String, String> outputsNameMap();

  /** Value class for our TensorFlow options. */
  @AutoValue
  public abstract static class Options implements Serializable {

    /**
     * Returns a list of Tags, see <a href=
     * "https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/saved_model/tag_constants.py#L26">tags</a>.
     */
    public abstract List<String> tags();

    public static Builder builder() {
      return new AutoValue_TensorFlowModel_Options.Builder();
    }

    /** Builder for enclosing Options. */
    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder tags(List<String> tags);

      public abstract Options build();
    }
  }

  private static Map<String, String> toNameMap(final Map<String, TensorInfo> infoMap) {
    return infoMap
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().getName()));
  }
}
