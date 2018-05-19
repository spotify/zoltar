/*-
 * -\-\-
 * mlengine-example
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

package com.spotify.zoltar.mlengine;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.ml.v1.CloudMachineLearningEngine;
import com.google.api.services.ml.v1.model.GoogleCloudMlV1PredictRequest;
import com.google.auto.value.AutoValue;
import com.google.common.io.BaseEncoding;
import com.spotify.zoltar.Model;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.tensorflow.example.Example;

/**
 * This model can be used to allow prediction on models deployed to Google Cloud ML Engine.
 * scikit-lean, XGBoost and TensorFlow models are supported.
 */
@AutoValue
public abstract class MlEngineModel implements Model<CloudMachineLearningEngine> {

  /**
   * Creates a Google Cloud ML Engine backed model.
   *
   * @param id {@link Model.Id} needs to be created with the following format:
   *           <pre>"projects/{PROJECT_ID}/models/{MODEL_ID}/versions/{MODEL_VERSION}"</pre>
   */
  public static MlEngineModel create(final Model.Id id) throws Exception {
    final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    final GoogleCredential credential = GoogleCredential.getApplicationDefault();
    final CloudMachineLearningEngine mlEngine = new CloudMachineLearningEngine
        .Builder(httpTransport, jsonFactory, null)
        .build();

    return new AutoValue_MlEngineModel(id, mlEngine, httpTransport, credential, jsonFactory);
  }

  abstract HttpTransport httpTransport();

  abstract GoogleCredential credentials();

  abstract JsonFactory jsonFactory();

  /**
   * Predict.
   *
   * @param data prediction input data.
   */
  public <T> List<T> predict(final List<?> data) throws IOException {
    final GoogleCloudMlV1PredictRequest predict =
        new GoogleCloudMlV1PredictRequest().set("instances", data);

    return (List<T>) instance().projects()
        .predict(id().value(), predict)
        .setAccessToken(credentials().getAccessToken())
        .execute()
        .get("predictions");
  }

  /**
   * Predict.
   *
   * @param examples TensorFlow {@link Example} input data.
   */
  public <T> List<T> predictExamples(final List<Example> examples) throws IOException {
    final List<Map<String, String>> data = examples.stream().map(example -> {
      final byte[] bytes = example.toByteArray();
      final String b64 = BaseEncoding.base64().encode(bytes);

      return Collections.singletonMap("b64", b64);
    }).collect(Collectors.toList());

    return (List<T>) predict(data);
  }

  /**
   * Close the model.
   */
  @Override
  public void close() throws Exception {
    httpTransport().shutdown();
  }

}
