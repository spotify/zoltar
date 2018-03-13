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

package com.spotify.zoltar.examples.apollo;

import com.spotify.apollo.Response;
import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Middlewares;
import com.spotify.apollo.route.Route;
import java.util.stream.Stream;
import okio.ByteString;

/**
 * Route endpoints.
 */
class EndPoints {

  Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
    final Stream<Route<AsyncHandler<Response<ByteString>>>> routes = Stream.of(
        Route.sync("GET", "/<features>", context ->
            IrisPrediction.predict(context.pathArgs().get("features")))
            .withMiddleware(EndPoints::serialize)
            .withDocString("Prediction handler", "Predicts the type of the iris"));
    return routes.map(r -> r.withMiddleware(Middlewares.apolloDefaults()));
  }

  private static AsyncHandler<Response<ByteString>> serialize(
      final AsyncHandler<Response<String>> handler) {
    return requestContext -> handler.invoke(requestContext)
        .thenApply(
            unserialized -> unserialized
                .withPayload(unserialized.payload().map(ByteString::encodeUtf8).orElse(null)));
  }
}
