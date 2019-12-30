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

// CHECKSTYLE:OFF
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tensorflow.example.Example;

import com.spotify.futures.CompletableFutures;
import com.spotify.zoltar.FeatureExtractFns.BatchExtractFn;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.IrisFeaturesSpec;
import com.spotify.zoltar.IrisFeaturesSpec.Iris;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Predictors;
import com.spotify.zoltar.featran.FeatranExtractFns;
import com.spotify.zoltar.tf.TensorFlowLoader;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(value = 5)
@Fork(
    value = 4,
    jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 20)
@Measurement(iterations = 20)
public class BenchmarkTensorFlow {

  private Predictor<Iris, Long> predictor;
  private Predictor<List<Iris>, Long> batchPredictor;
  private List<IrisFeaturesSpec.Iris> data;

  public static void main(String[] args) throws RunnerException {
    final Options opt =
        new OptionsBuilder().include(BenchmarkTensorFlow.class.getSimpleName()).build();

    new Runner(opt).run();
  }

  @Setup
  public void setup() throws Exception {
    data = IrisHelper.getIrisData();
    batchPredictor = batchPredictor();
    predictor = predictor();
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  public void single() throws ExecutionException, InterruptedException {
    final List<CompletionStage<List<Prediction<Iris, Long>>>> predictions =
        data.stream().map(predictor::predict).collect(Collectors.toList());

    CompletableFutures.allAsList(predictions)
        .thenApply(l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList()))
        .get();
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  public void batch() throws ExecutionException, InterruptedException {
    batchPredictor.predict(data).toCompletableFuture().get();
  }

  @TearDown
  public void shutdown() {
    predictor.timeoutScheduler().scheduler().shutdown();
    batchPredictor.timeoutScheduler().scheduler().shutdown();
  }

  public static Predictor<List<Iris>, Long> batchPredictor() throws Exception {
    final String modelUri =
        BenchmarkTensorFlow.class.getResource("/trained_model").toURI().toString();
    final URI settingsUri = BenchmarkTensorFlow.class.getResource("/settings.json").toURI();
    final String settings =
        new String(Files.readAllBytes(Paths.get(settingsUri)), StandardCharsets.UTF_8);

    final ModelLoader<TensorFlowModel> modelLoader = TensorFlowLoader.create(modelUri);

    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(IrisFeaturesSpec.irisFeaturesSpec(), settings);
    final BatchExtractFn<Iris, Example> batch = BatchExtractFn.lift(extractFn);

    final String op = "linear/head/predictions/class_ids";
    final TensorFlowPredictFn<List<Iris>, List<Example>, Long> predictFn =
        TensorFlowPredictFn.exampleBatch(tensors -> tensors.get(op).longValue()[0], op);

    return Predictors.newBuilder(modelLoader, FeatureExtractor.create(batch), predictFn)
        .predictor();
  }

  public static Predictor<Iris, Long> predictor() throws Exception {
    final String modelUri =
        BenchmarkTensorFlow.class.getResource("/trained_model").toURI().toString();
    final URI settingsUri = BenchmarkTensorFlow.class.getResource("/settings.json").toURI();
    final String settings =
        new String(Files.readAllBytes(Paths.get(settingsUri)), StandardCharsets.UTF_8);
    final ExtractFn<Iris, Example> extractFn =
        FeatranExtractFns.example(IrisFeaturesSpec.irisFeaturesSpec(), settings);

    final String op = "linear/head/predictions/class_ids";
    return Predictors.tensorFlow(
        modelUri, extractFn, tensors -> tensors.get(op).longValue()[0], op);
  }
}
