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

import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.Model;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.framework.ConfigProto;

/**
 * This model can be used to load protobuf definition of a TensorFlow graph. See:
 * https://github.com/spotify/spotify-tensorflow/blob/master/spotify_tensorflow/freeze_graph.py
 * TensorFlowGraphModel is thread-safe.
 */
public class TensorFlowGraphModel<T> implements Model<Session, T>, AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(TensorFlowGraphModel.class);
  private final Graph graph;
  private final Session session;
  private final String settings;
  private final JFeatureSpec<T> featureSpec;

  /**
   * Creates a TensorFlow model based on a frozen, serialized TensorFlow graph.
   *
   * @param graphDef byte array representing the TensorFlow graph definition
   * @param config ConfigProto config for TensorFlow session
   * @param prefix a prefix that will be prepended to names in graphDef
   * @param settingsResource URI to Featran settings
   * @param featureSpec Featran's FeatureSpec
   */
  public static <T> TensorFlowGraphModel<T> from(final byte[] graphDef,
                                                 @Nullable final ConfigProto config,
                                                 @Nullable final String prefix,
                                                 final URI settingsResource,
                                                 final JFeatureSpec<T> featureSpec)
      throws IOException {
    return new TensorFlowGraphModel(graphDef, config, prefix, settingsResource, featureSpec);
  }

  /**
   * Creates a TensorFlow model based on a frozen, serialized TensorFlow graph.
   *
   * @param graphUri URI to the TensorFlow graph definition
   * @param config ConfigProto config for TensorFlow session
   * @param prefix a prefix that will be prepended to names in graphDef
   * @param settingsResource URI to Featran settings
   * @param featureSpec Featran's FeatureSpec
   */
  public static <T> TensorFlowGraphModel<T> from(final URI graphUri,
                                                 @Nullable final ConfigProto config,
                                                 @Nullable final String prefix,
                                                 final URI settingsResource,
                                                 final JFeatureSpec<T> featureSpec)
      throws IOException {
    byte[] graphBytes = Files.readAllBytes(Paths.get(graphUri));
    return new TensorFlowGraphModel(graphBytes, config, prefix, settingsResource, featureSpec);
  }

  private TensorFlowGraphModel(final byte[] graphDef,
                               @Nullable final ConfigProto config,
                               @Nullable final String prefix,
                               final URI settingsResource,
                               final JFeatureSpec<T> featureSpec) throws IOException {
    settings = new String(Files.readAllBytes(Paths.get(settingsResource)),
                          StandardCharsets.UTF_8);
    graph = new Graph();
    session = new Session(graph, config != null ? config.toByteArray() : null);
    this.featureSpec = featureSpec;
    final long loadStart = System.currentTimeMillis();
    if (prefix == null) {
      logger.debug("Loading graph definition without prefix");
      graph.importGraphDef(graphDef);
    } else {
      logger.debug("Loading graph definition with prefix: %s", prefix);
      graph.importGraphDef(graphDef, prefix);
    }
    logger.info("TensorFlow graph loaded in %d ms", System.currentTimeMillis() - loadStart);
  }


  @Override
  public void close() {
    if (session != null) {
      logger.debug("Closing TensorFlow session");
      session.close();
    }
    if (graph != null) {
      logger.debug("Closing TensorFlow graph");
      graph.close();
    }
  }

  /**
   * Returns TensorFlow graph.
   */
  public Graph graph() {
    return graph;
  }

  @Override
  public Session instance() {
    return session;
  }

  @Override
  public String settings() {
    return settings;
  }

  @Override
  public JFeatureSpec<T> featureSpec() {
    return featureSpec;
  }
}
