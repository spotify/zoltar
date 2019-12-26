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
package com.spotify.zoltar;

import static com.spotify.zoltar.fs.FileSystemExtrasTestUtils.jarUri;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.common.util.concurrent.MoreExecutors;

import com.spotify.zoltar.tf.TensorFlowGraphLoader;
import com.spotify.zoltar.tf.TensorFlowGraphModel;
import com.spotify.zoltar.tf.TensorFlowLoader;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.xgboost.XGBoostLoader;
import com.spotify.zoltar.xgboost.XGBoostModel;

public class LoaderIT {

  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  @Test
  public void tfLoaderGcs() throws Exception {
    // Load directory from GCS without trailing slash. GCS requires trailing slash for
    // directories, so Zoltar should append it for us.
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/tensorflow/export";
    final TensorFlowModel model =
        TensorFlowLoader.create(uri, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test
  public void tfLoaderGcsTrailingSlash() throws Exception {
    // Load directory from GCS with trailing slash, which GCS requires for directories.
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/tensorflow/export/";
    final TensorFlowModel model =
        TensorFlowLoader.create(uri, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test
  public void tfLoaderJar() throws Exception {
    // Load directory from jar file without trailing slash.
    final URI uri = jarUri();
    final TensorFlowModel model =
        TensorFlowLoader.create(uri.toString(), MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test(expected = ExecutionException.class)
  public void tfLoaderJarTrailingSlash() throws Exception {
    // Load directory from jar file with trailing slash. On Java 1.8 this will throw an exception
    // because of ZipPath.relativize. See https://github.com/spotify/zoltar/pull/176 for more info.
    // It will not throw an exception on Java 11.
    final URI uri = jarUri();
    final TensorFlowModel model =
        TensorFlowLoader.create(uri.toString() + "/", MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test
  public void tfGraphLoader() throws Exception {
    // Load a file from GCS without trailing slash
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/tensorflowGraph/tfgraph.bin";
    final TensorFlowGraphModel model =
        TensorFlowGraphLoader.create(uri, null, null, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test(expected = ExecutionException.class)
  public void tfGraphLoaderTrailingSlash() throws Exception {
    // Load a file from GCS with trailing slash. This should throw an exception because GCS does
    // not allow a trailing slash on files.
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/tensorflowGraph/tfgraph.bin/";
    final TensorFlowGraphModel model =
        TensorFlowGraphLoader.create(uri, null, null, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test
  public void xgBoostLoader() throws Exception {
    // Load a file from GCS without trailing slash
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/xgBoost/model.xgb";
    final XGBoostModel model =
        XGBoostLoader.create(uri, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }

  @Test(expected = ExecutionException.class)
  public void xgBoostLoaderTrailingSlash() throws Exception {
    // Load a file from GCS with trailing slash. This should throw an exception because GCS does
    // not allow a trailing slash on files.
    final String uri = "gs://data-integration-test-us/zoltar/LoaderIT/xgBoost/model.xgb/";
    final XGBoostModel model =
        XGBoostLoader.create(uri, MoreExecutors.directExecutor()).get(TIMEOUT);
    assertThat(model, notNullValue());
  }
}
