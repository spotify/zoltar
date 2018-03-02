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

package com.spotify.mlservingexample;

import com.spotify.apollo.Environment;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.logging.request.StructuredLoggingModule;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.Predictor;
import com.spotify.modelserving.tf.TensorFlowModel;
import com.typesafe.config.Config;
import java.io.IOException;

/**
 * Application entry point.
 */
public class ServiceRunner {

  static final String SERVICE_NAME = "ml-serving-example";

  private ServiceRunner() { }

  /**
   * Runs the app locally.
   *
   * <p>$ curl http://localhost:8080/predict/5.8-2.7-5.1-1.9
   * Lengths seperated by "-"
   */
  public static void main(final String... args) throws LoadingException {

    Service service = HttpService.usingAppInit(ServiceRunner::configure, SERVICE_NAME)
        .withEnvVarPrefix("SPOTIFY")
        .withModule(new StructuredLoggingModule())
        .build();

    HttpService.boot(service, args);
  }

  static void configure(final Environment environment) {

    final Config config = environment.config();
    try {
      TensorFlowModel<Iris> model = IrisModel.loadModel(config.getString("iris.model"));
      Predictor<Iris, Long> predictor = IrisPredictor.loadPredictor(model);
      IrisPrediction.setPredictor(predictor);
    } catch (IOException e) {
      e.printStackTrace();
    }

    final EndPoints endPoints = new EndPoints();
    environment.routingEngine()
        .registerRoutes(endPoints.routes().map(r -> r.withPrefix("/predict")));
  }
}
