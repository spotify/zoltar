/*-
 * -\-\-
 * zoltar-featran
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

package com.spotify.modelserving.featran;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.DoubleSparseArray;
import com.spotify.featran.java.FloatSparseArray;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.tensorflow.example.Example;

public class Featran {

  public static <T> JRecordExtractor<T, float[]> loadFloatExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    final String settings = new String(Files.readAllBytes(Paths.get(path)));
    return JFeatureSpec.wrap(spec).extractWithSettingsFloat(settings);
  }

  public static <T> JRecordExtractor<T, double[]> loadDoubleExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    final String settings = new String(Files.readAllBytes(Paths.get(path)));
    return JFeatureSpec.wrap(spec).extractWithSettingsDouble(settings);
  }

  public static <T> JRecordExtractor<T, FloatSparseArray> loadFloatSparseArrayExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    final String settings = new String(Files.readAllBytes(Paths.get(path)));
    return JFeatureSpec.wrap(spec).extractWithSettingsFloatSparseArray(settings);
  }

  public static <T> JRecordExtractor<T, DoubleSparseArray> loadDoubleSparseArrayExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    final String settings = new String(Files.readAllBytes(Paths.get(path)));
    return JFeatureSpec.wrap(spec).extractWithSettingsDoubleSparseArray(settings);
  }

  public static <T> JRecordExtractor<T, Example> loadExampleExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    final String settings = new String(Files.readAllBytes(Paths.get(path)));
    return JFeatureSpec.wrap(spec).extractWithSettingsExample(settings);
  }
}
