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
package com.spotify.zoltar.jmh;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tensorflow.ndarray.LongNdArray;
import org.tensorflow.proto.example.Example;
import org.tensorflow.types.TInt64;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.featran.FeatranExtractFns;

/** TensorFlow prediction benchmarks. */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Threads(value = 1)
@Fork(value = 4)
// @Warmup(iterations = 20)
// @Measurement(iterations = 20)
public class BenchmarkTensorFlow {
  @Param({"1", "100"})
  private int size;

  private Predictor<Iris, Long> predictor;
  private Iris[] data;

  /** run benchmarks. */
  public static void main(final String[] args) throws RunnerException {
    final Options opt =
        new OptionsBuilder().include(BenchmarkTensorFlow.class.getSimpleName()).build();

    new Runner(opt).run();
  }

  /** fetch benchmark data and initialize predictors. */
  @Setup
  public void setup() throws Exception {
    data = Arrays.copyOf(IrisHelper.getIrisData(), size);
    predictor = predictor();
  }

  /** input prediction. right now the batch size matches the benchmark size param. */
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public void predict() throws ExecutionException, InterruptedException {
    predictor.predict(data).toCompletableFuture().get();
  }

  @TearDown
  public void shutdown() {
    predictor.timeoutScheduler().scheduler().shutdown();
  }

  private static ExtractFn<Iris, Example> extractFn() throws IOException, URISyntaxException {
    final URI settingsUri = BenchmarkTensorFlow.class.getResource("/settings.json").toURI();
    final String settings =
        new String(Files.readAllBytes(Paths.get(settingsUri)), StandardCharsets.UTF_8);

    return FeatranExtractFns.example(IrisFeaturesSpec.irisFeaturesSpec(), settings);
  }

  private static Predictor<Iris, Long> predictor() throws Exception {
    final String op = "linear/head/predictions/class_ids";
    final String modelUri =
        BenchmarkTensorFlow.class.getResource("/trained_model").toURI().toString();
    return Predictors.tensorFlow(
        modelUri,
        extractFn(),
        tensors -> {
          final TInt64 data = (TInt64) tensors.get(op);
          return StreamSupport.stream(data.scalars().spliterator(), false)
              .map(LongNdArray::getObject)
              .collect(Collectors.toList());
        },
        op);
  }
}
