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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import okio.ByteString;
import scala.Option;

import com.google.common.collect.ImmutableMap;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Middlewares;
import com.spotify.apollo.route.Route;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;

/** Route endpoints. */
final class IrisPredictionHandler {

  private static final Map<Long, String> idToClass =
      ImmutableMap.of(
          0L, "Iris-setosa",
          1L, "Iris-versicolor",
          2L, "Iris-virginica");

  private IrisPredictionHandler(final Predictor<Iris, Long> predictor) {
    this.predictor = predictor;
  }

  private final Predictor<Iris, Long> predictor;

  static IrisPredictionHandler create(final Predictor<Iris, Long> predictor) {
    return new IrisPredictionHandler(predictor);
  }

  Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
    final Stream<Route<AsyncHandler<Response<ByteString>>>> routes =
        Stream.of(
            Route.async("GET", "/predict/<features>", this::predict)
                .withDocString("Prediction handler", "Predicts the type of the iris"));
    return routes.map(r -> r.withMiddleware(Middlewares.apolloDefaults()));
  }

  /**
   * Prediction endpoint. Takes a request in a create of a String containing iris features `-`
   * separated, and returns a response in a form of a predicted iris class.
   */
  CompletionStage<Response<ByteString>> predict(final RequestContext context) {
    return Optional.ofNullable(context.pathArgs().get("features"))
        .map(f -> f.split("-"))
        .filter(features -> features.length == 4)
        .map(this::predict)
        .map(p -> p.thenApply(ByteString::encodeUtf8))
        .map(p -> p.thenApply(Response::forPayload))
        .orElse(CompletableFuture.completedFuture(Response.forStatus(Status.BAD_REQUEST)));
  }

  /**
   * Prediction endpoint. Takes a request in a create of a String containing iris features `-`
   * separated, and returns a response in a form of a predicted iris class.
   */
  CompletionStage<String> predict(final String[] features) {
    final Iris featureData =
        new Iris(
            Option.apply(Double.parseDouble(features[0])),
            Option.apply(Double.parseDouble(features[1])),
            Option.apply(Double.parseDouble(features[2])),
            Option.apply(Double.parseDouble(features[3])),
            Option.empty());

    return predictor
        .predict(featureData)
        .thenApply(
            ps -> {
              return ps.stream()
                  .findFirst()
                  .map(Prediction::value)
                  .map(idToClass::get)
                  .orElseThrow(() -> new RuntimeException("we expect a prediction"));
            });
  }
}
