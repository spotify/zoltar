/*-
 * -\-\-
 * apollo-service-example
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

package com.spotify.mlservingexample;

import com.spotify.apollo.Environment;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.logging.request.StructuredLoggingModule;
import com.typesafe.config.Config;
import java.io.IOException;
import java.net.URI;

/**
 * Application entry point.
 */
public class ServiceRunner {

  private static final String SERVICE_NAME = "ml-serving-example";

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
    URI modelPath = URI.create(config.getString("iris.model"));
    URI settingsPath = URI.create(config.getString("iris.settings"));
    try {
      IrisPrediction.configure(modelPath, settingsPath);
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Could not load model! Model path: `%s`, settings path `%s`.",
              modelPath,
              settingsPath));
    }

    final EndPoints endPoints = new EndPoints();
    environment.routingEngine()
        .registerRoutes(endPoints.routes().map(r -> r.withPrefix("/predict")));
  }
}
