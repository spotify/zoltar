/*
 * Copyright (C) 2016 - 2018 Spotify AB
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
package com.spotify.zoltar.examples.metrics;

import java.util.List;
import java.util.stream.Collectors;

import com.codahale.metrics.Counter;
import com.google.auto.value.AutoValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.Prediction;
import com.spotify.zoltar.Predictor;
import com.spotify.zoltar.Vector;
import com.spotify.zoltar.metrics.FeatureExtractorMetrics;
import com.spotify.zoltar.metrics.Instrumentations;
import com.spotify.zoltar.metrics.PredictFnMetrics;
import com.spotify.zoltar.metrics.PredictMetrics;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.metrics.VectorMetrics;
import com.spotify.zoltar.metrics.semantic.SemanticPredictorMetrics;

/** Example showing how to add custom metrics to a Predictor. */
class CustomMetricsExample implements Predictor<DummyModel, Integer, Float, Float> {

  private Predictor<DummyModel, Integer, Float, Float> predictor;

  /** Define a class containing all the additional metrics we want to register. */
  @AutoValue
  abstract static class CustomMetrics {

    abstract Counter negativePredictCount();

    abstract Counter negativeExtractCount();

    static CustomMetrics create(final SemanticMetricRegistry registry, final MetricId metricId) {
      final MetricId predictCountId = metricId.tagged("what", "negativePredictCount");
      final MetricId extractCountId = metricId.tagged("what", "negativeExtractCount");
      final Counter negativePredictCount = registry.counter(predictCountId);
      final Counter negativeExtractCount = registry.counter(extractCountId);

      return new AutoValue_CustomMetricsExample_CustomMetrics(
          negativePredictCount, negativeExtractCount);
    }
  }

  /**
   * Define an implementation of PredictorMetrics, which will hold metrics for feature extraction
   * and prediction. In this example we don't make use of the model ID.
   */
  @AutoValue
  abstract static class CustomPredictorMetrics implements PredictorMetrics<Integer, Float, Float> {

    abstract LoadingCache<Model.Id, CustomMetrics> metricsCache();

    static CustomPredictorMetrics create(
        final SemanticMetricRegistry registry, final MetricId metricId) {
      final LoadingCache<Model.Id, CustomMetrics> metersCache =
          CacheBuilder.<Model.Id, CustomMetrics>newBuilder()
              .build(
                  new CacheLoader<Model.Id, CustomMetrics>() {
                    @Override
                    public CustomMetrics load(final Model.Id id) {
                      return CustomMetrics.create(registry, metricId.tagged("model", id.value()));
                    }
                  });

      return new AutoValue_CustomMetricsExample_CustomPredictorMetrics(metersCache);
    }

    @Override
    public PredictFnMetrics<Integer, Float> predictFnMetrics() {
      return id -> {
        final CustomMetrics metrics = metricsCache().getUnchecked(id);
        final Counter negativePredictCounter = metrics.negativePredictCount();
        return NegativePredictMetrics.create(negativePredictCounter);
      };
    }

    @Override
    public FeatureExtractorMetrics<Integer, Float> featureExtractorMetrics() {
      return id -> {
        final CustomMetrics metrics = metricsCache().getUnchecked(id);
        final Counter negativeExtractCounter = metrics.negativeExtractCount();
        return NegativeExtractMetrics.create(negativeExtractCounter);
      };
    }
  }

  /** To define a metric you want to measure for prediction, implement PredictMetrics. */
  @AutoValue
  abstract static class NegativePredictMetrics implements PredictMetrics<Integer, Float> {

    abstract Counter negativePredictCount();

    static NegativePredictMetrics create(final Counter negativeCount) {
      return new AutoValue_CustomMetricsExample_NegativePredictMetrics(negativeCount);
    }

    /** Here, given a list of predictions, we want to measure the number of negatives. */
    @Override
    public void prediction(final List<Prediction<Integer, Float>> predictions) {
      negativePredictCount().inc(predictions.stream().filter(x -> x.value() < 0).count());
    }
  }

  /** To define a metric you want to measure for feature extraction, implement VectorMetrics. */
  @AutoValue
  abstract static class NegativeExtractMetrics implements VectorMetrics<Integer, Float> {

    abstract Counter negativeExtractCount();

    static NegativeExtractMetrics create(final Counter negativeCount) {
      return new AutoValue_CustomMetricsExample_NegativeExtractMetrics(negativeCount);
    }

    /**
     * Here, given a list of feature extraction results, we want to count the number of negatives.
     */
    @Override
    public void extraction(final List<Vector<Integer, Float>> vectors) {
      negativeExtractCount().inc(vectors.stream().filter(v -> v.value() < 0).count());
    }
  }

  CustomMetricsExample(final SemanticMetricRegistry metricRegistry, final MetricId metricId) {
    final ModelLoader<DummyModel> modelLoader = ModelLoader.loaded(new DummyModel());
    final ExtractFn<Integer, Float> extractFn = ExtractFn.lift(input -> (float) input / 10);
    final PredictFn<DummyModel, Integer, Float, Float> predictFn =
        (model, vectors) -> {
          return vectors
              .stream()
              .map(vector -> Prediction.create(vector.input(), vector.value() * 2))
              .collect(Collectors.toList());
        };

    // We build the Predictor as usual, compose with the built-in metrics, and then compose
    // with our custom metrics.
    // #PredictorMetrics
    final PredictorMetrics<Integer, Float, Float> predictorMetrics =
        SemanticPredictorMetrics.create(metricRegistry, metricId);
    // #PredictorMetrics

    final PredictorMetrics<Integer, Float, Float> customMetrics =
        CustomPredictorMetrics.create(metricRegistry, metricId);

    // #PredictorWithMetrics
    predictor =
        Predictor.<DummyModel, Integer, Float, Float>builder()
            .modelLoader(modelLoader)
            .featureExtractFn(extractFn)
            .predictFn(predictFn)
            .build()
            .with(Instrumentations.predictor(predictorMetrics))
            .with(Instrumentations.predictor(customMetrics));
    // #PredictorWithMetrics
  }

  @Override
  public ModelLoader<DummyModel> modelLoader() {
    return predictor.modelLoader();
  }

  @Override
  public FeatureExtractor<DummyModel, Integer, Float> featureExtractor() {
    return predictor.featureExtractor();
  }

  @Override
  public AsyncPredictFn<DummyModel, Integer, Float, Float> predictFn() {
    return predictor.predictFn();
  }
}
