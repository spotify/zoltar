/*-
 * -\-\-
 * model-serving-xgboost
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

package com.spotify.modelserving.xgboost;

import com.spotify.modelserving.Model;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.GompLoader;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class XGBoostModel implements Model<Booster> {

  private final Booster booster;

  private XGBoostModel(Booster booster) {
    this.booster = booster;
  }

  public static XGBoostModel create(String modelUri) throws IOException {
    return create(URI.create(modelUri));
  }

  public static XGBoostModel create(URI modelUri) throws IOException {
    try {
      GompLoader.start();
      final InputStream is = Files.newInputStream(Paths.get(modelUri));
      return new XGBoostModel(XGBoost.loadModel(is));
    } catch (XGBoostError xgBoostError) {
      throw new IOException(xgBoostError);
    }
  }

  @Override
  public Booster instance() {
    return booster;
  }

  @Override
  public void close() throws Exception {

  }
}
