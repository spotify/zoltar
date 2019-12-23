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
package com.spotify.zoltar.examples.apollo;

import java.io.IOException;
import java.util.stream.Stream;

import okio.ByteString;

import com.typesafe.config.Config;

import com.spotify.apollo.Environment;
import com.spotify.apollo.Response;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Route;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.metrics.ffwd.FastForwardReporter;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.metrics.semantic.SemanticPredictorMetrics;

/** Application entry point. */
public class App {

  private static final String SERVICE_NAME = "zoltar-example";

  private App() {}

  static void configure(final Environment environment) {
    final Config config = environment.config();

    final SemanticMetricRegistry metricRegistry = environment.resolve(SemanticMetricRegistry.class);
    final MetricId serviceMetricId = MetricId.build().tagged("service", SERVICE_NAME);

    final PredictorMetrics metrics =
        SemanticPredictorMetrics.create(metricRegistry, serviceMetricId);

    try {
      // Optional: check out https://github.com/spotify/semantic-metrics#provided-plugins
      final FastForwardReporter reporter = FastForwardReporter.forRegistry(metricRegistry).build();
      reporter.start();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage());
    }

    final Predictor<Iris, Long> predictor;
    try {
      final ModelConfig irisModelConfig = ModelConfig.from(config.getConfig("iris"));
      predictor = IrisPredictor.create(irisModelConfig, metrics);
    } catch (final Exception e) {
      throw new RuntimeException("Could not load model with config");
    }

    final IrisPredictionHandler irisPredictionHandler = IrisPredictionHandler.create(predictor);
    final Stream<Route<AsyncHandler<Response<ByteString>>>> routes =
        irisPredictionHandler.routes().map(r -> r.withPrefix("/v1"));

    environment.routingEngine().registerRoutes(routes);
  }

  /**
   * Runs the app locally.
   *
   * <p>$ curl http://localhost:8080/v1/predict/5.8-2.7-5.1-1.9 Lengths seperated by "-"
   */
  public static void main(final String... args) throws LoadingException {

    final Service service = HttpService.usingAppInit(App::configure, SERVICE_NAME).build();

    HttpService.boot(service, args);
  }
}
