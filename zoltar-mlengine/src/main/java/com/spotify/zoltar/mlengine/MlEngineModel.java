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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.tensorflow.proto.example.Example;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.ml.v1.CloudMachineLearningEngine;
import com.google.api.services.ml.v1.CloudMachineLearningEngineScopes;
import com.google.api.services.ml.v1.model.GoogleApiHttpBody;
import com.google.api.services.ml.v1.model.GoogleCloudMlV1PredictRequest;
import com.google.auto.value.AutoValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.BaseEncoding;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.mlengine.MlEngineModel.Response.Predictions;

/**
 * This model can be used to allow prediction on models deployed to Google Cloud ML Engine.
 * scikit-learn, XGBoost and TensorFlow models are supported.
 */
@AutoValue
public abstract class MlEngineModel implements Model<CloudMachineLearningEngine> {

  private static final String APPLICATION_NAME = "zoltar";

  /**
   * Creates a Google Cloud ML Engine backed model.
   *
   * @param projectId Google project id.
   * @param modelId model id.
   */
  static MlEngineModel create(final String projectId, final String modelId)
      throws IOException, GeneralSecurityException {
    final String id = String.format("projects/%s/models/%s", projectId, modelId);
    return create(Model.Id.create(id));
  }

  /**
   * Creates a Google Cloud ML Engine backed model.
   *
   * @param projectId Google project id.
   * @param modelId model id.
   * @param versionId model version id.
   */
  static MlEngineModel create(final String projectId, final String modelId, final String versionId)
      throws IOException, GeneralSecurityException {
    final String id =
        String.format("projects/%s/models/%s/versions/%s", projectId, modelId, versionId);
    return create(Model.Id.create(id));
  }

  /**
   * Creates a Google Cloud ML Engine backed model.
   *
   * @param id {@link Model.Id} needs to be created with the following format:
   *     <pre>"projects/{PROJECT_ID}/models/{MODEL_ID}"</pre>
   *     or
   *     <pre>"projects/{PROJECT_ID}/models/{MODEL_ID}/versions/{MODEL_VERSION}"</pre>
   */
  public static MlEngineModel create(final Model.Id id)
      throws IOException, GeneralSecurityException {
    final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    final JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
    final GoogleCredential credential =
        GoogleCredential.getApplicationDefault()
            .createScoped(CloudMachineLearningEngineScopes.all());

    final CloudMachineLearningEngine mlEngine =
        new CloudMachineLearningEngine.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();

    return new AutoValue_MlEngineModel(id, mlEngine, httpTransport);
  }

  abstract HttpTransport httpTransport();

  /**
   * Predict.
   *
   * @param data prediction input data.
   */
  public Predictions predict(final List<?> data) throws IOException, MlEnginePredictException {
    final GoogleCloudMlV1PredictRequest predict =
        new GoogleCloudMlV1PredictRequest().set("instances", data);
    final GoogleApiHttpBody httpBody =
        instance().projects().predict(id().value(), predict).execute();

    final Response response = Response.from(httpBody);
    return response
        .predictions()
        .orElseThrow(
            () -> {
              return response.error().map(MlEnginePredictException::new).get();
            });
  }

  /**
   * Predict.
   *
   * @param examples TensorFlow {@link Example} input data.
   */
  public Predictions predictExamples(final List<Example> examples)
      throws IOException, MlEnginePredictException {
    final List<Map<String, String>> data =
        examples
            .stream()
            .map(
                example -> {
                  final byte[] bytes = example.toByteArray();
                  final String b64 = BaseEncoding.base64().encode(bytes);

                  return Collections.singletonMap("b64", b64);
                })
            .collect(Collectors.toList());

    return predict(data);
  }

  /** Close the model. */
  @Override
  public void close() throws Exception {
    httpTransport().shutdown();
  }

  /** Prediction response. */
  @AutoValue
  public abstract static class Response {

    abstract GoogleApiHttpBody content();

    static Response from(final GoogleApiHttpBody content) {
      return new AutoValue_MlEngineModel_Response(content);
    }

    /**
     * List of predictions. Return type depends on the model used.
     *
     * @see <a
     *     href="https://cloud.google.com/ml-engine/docs/v1/predict-request">https://cloud.google.com/ml-engine/docs/v1/predict-request</a>
     */
    @SuppressWarnings("unchecked")
    public Optional<Predictions> predictions() {
      final List<Object> predictions =
          (List<Object>) content().getOrDefault("predictions", Collections.emptyList());

      if (predictions.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(Predictions.create(predictions));
    }

    /** Prediction error. */
    public Optional<String> error() {
      return Optional.ofNullable((String) content().get("error"));
    }

    /** Holds the predictions values. */
    @AutoValue
    public abstract static class Predictions {

      private static final ObjectMapper MAPPER = new ObjectMapper();
      private static final Cache<Class<?>, JavaType> CACHE = CacheBuilder.newBuilder().build();

      /**
       * List of predictions. Return type depends on the model used.
       *
       * @see <a
       *     href="https://cloud.google.com/ml-engine/docs/v1/predict-request">https://cloud.google.com/ml-engine/docs/v1/predict-request</a>
       */
      public abstract List<Object> values();

      /**
       * List of predictions.
       *
       * @param klass class to each returned prediction objects are converted.
       */
      public <T> List<T> values(final Class<T> klass) throws ExecutionException {
        final JavaType javaType =
            CACHE.get(klass, () -> MAPPER.getTypeFactory().constructType(klass));

        return values()
            .stream()
            .map(p -> MAPPER.<T>convertValue(p, javaType))
            .collect(Collectors.toList());
      }

      static Predictions create(final List<Object> values) {
        return new AutoValue_MlEngineModel_Response_Predictions(values);
      }
    }
  }
}
