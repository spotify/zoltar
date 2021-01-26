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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.tensorflow.Tensor;
import org.tensorflow.proto.example.Example;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.PredictFns.AsyncPredictFn;
import com.spotify.zoltar.PredictFns.PredictFn;
import com.spotify.zoltar.metrics.Instrumentations;
import com.spotify.zoltar.metrics.PredictorMetrics;
import com.spotify.zoltar.tf.TensorFlowLoader;
import com.spotify.zoltar.tf.TensorFlowModel;
import com.spotify.zoltar.tf.TensorFlowPredictFn;

/**
 * This class consists exclusively of static methods that return {@link PredictorBuilder} or {@link
 * Predictor}.
 *
 * <p>This is the public entry point for Predictors.
 */
public final class Predictors {

  private Predictors() {}

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor}and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn);
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor}and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
          final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn)
        .with(Instrumentations.predictor(metrics));
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * AsyncPredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final ExtractFn<InputT, VectorT> extractFn,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
          final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return DefaultPredictorBuilder.create(modelLoader, extractFn, predictFn)
        .with(Instrumentations.predictor(metrics));
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link PredictFn}.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final PredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
          final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn)
        .with(Instrumentations.predictor(metrics));
  }

  /**
   * Returns a PredictorBuilder given a {@link Model}, {@link FeatureExtractor} and a {@link
   * PredictFn}.
   *
   * @param modelLoader model loader that loads the model to perform prediction on.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param predictFn a prediction function to perform prediction with {@link AsyncPredictFn}.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <ModelT> underlying type of the {@link Model}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <VectorT> type of the output from {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static <ModelT extends Model<?>, InputT, VectorT, ValueT>
      PredictorBuilder<ModelT, InputT, VectorT, ValueT> newBuilder(
          final ModelLoader<ModelT> modelLoader,
          final FeatureExtractor<ModelT, InputT, VectorT> featureExtractor,
          final AsyncPredictFn<ModelT, InputT, VectorT, ValueT> predictFn,
          final PredictorMetrics<InputT, VectorT, ValueT> metrics) {
    return DefaultPredictorBuilder.create(modelLoader, featureExtractor, predictFn)
        .with(Instrumentations.predictor(metrics));
  }

  /**
   * Returns a TensorFlow Predictor.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param outTensorExtractor function to extract the output value from a {@link Tensor}.
   * @param fetchOps operations to fetch.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  public static <InputT, ValueT> Predictor<InputT, ValueT> tensorFlow(
      final String modelUri,
      final ExtractFn<InputT, Example> extractFn,
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String... fetchOps) {
    return tensorFlow(modelUri, FeatureExtractor.create(extractFn), outTensorExtractor, fetchOps);
  }

  /**
   * Returns a TensorFlow Predictor.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param extractFn a feature extract function to use to transform input into extracted features.
   * @param outTensorExtractor function to extract the output value from a {@link Tensor}.
   * @param fetchOps operations to fetch.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  public static <InputT, ValueT> Predictor<InputT, ValueT> tensorFlow(
      final String modelUri,
      final ExtractFn<InputT, Example> extractFn,
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String[] fetchOps,
      final PredictorMetrics<InputT, Example, ValueT> metrics) {
    return tensorFlow(
        modelUri, FeatureExtractor.create(extractFn), outTensorExtractor, fetchOps, metrics);
  }

  /**
   * Returns a TensorFlow Predictor.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param outTensorExtractor function to extract the output value from a {@link Tensor}.
   * @param fetchOps operations to fetch.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  public static <InputT, ValueT> Predictor<InputT, ValueT> tensorFlow(
      final String modelUri,
      final FeatureExtractor<TensorFlowModel, InputT, Example> featureExtractor,
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String... fetchOps) {
    final ModelLoader<TensorFlowModel> modelLoader = TensorFlowLoader.create(modelUri);
    final TensorFlowPredictFn<InputT, Example, ValueT> predictFn =
        TensorFlowPredictFn.example(outTensorExtractor, fetchOps);

    return newBuilder(modelLoader, featureExtractor, predictFn).predictor();
  }

  /**
   * Returns a TensorFlow Predictor.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param featureExtractor a feature extractor to use to transform input into extracted features.
   * @param outTensorExtractor function to extract the output value from a {@link Tensor}.
   * @param fetchOps operations to fetch.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <InputT> type of the input to the {@link FeatureExtractor}.
   * @param <ValueT> type of the prediction result.
   */
  public static <InputT, ValueT> Predictor<InputT, ValueT> tensorFlow(
      final String modelUri,
      final FeatureExtractor<TensorFlowModel, InputT, Example> featureExtractor,
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String[] fetchOps,
      final PredictorMetrics<InputT, Example, ValueT> metrics) {
    final ModelLoader<TensorFlowModel> modelLoader = TensorFlowLoader.create(modelUri);
    final TensorFlowPredictFn<InputT, Example, ValueT> predictFn =
        TensorFlowPredictFn.example(outTensorExtractor, fetchOps);

    return newBuilder(modelLoader, featureExtractor, predictFn, metrics).predictor();
  }

  /**
   * Returns a TensorFlow Predictor. Assumes feature extraction is embedded in the model via
   * Tensorflow Transform, so no extractFn is needed and the input type must be Example.
   *
   * @param modelUri should point to a directory of the saved TensorFlow {@link
   *     org.tensorflow.SavedModelBundle}, can be a URI to a local filesystem, resource, GCS etc.
   * @param outTensorExtractor function to extract the output value from a {@link Tensor}.
   * @param fetchOps operations to fetch.
   * @param metrics a predictor metrics implementation {@link
   *     com.spotify.zoltar.metrics.semantic.SemanticPredictMetrics}.
   * @param <ValueT> type of the prediction result.
   */
  public static <ValueT> Predictor<Example, ValueT> tensorFlow(
      final String modelUri,
      final Function<Map<String, Tensor<?>>, List<ValueT>> outTensorExtractor,
      final String[] fetchOps,
      final PredictorMetrics<Example, Example, ValueT> metrics) {
    return tensorFlow(modelUri, ExtractFn.identity(), outTensorExtractor, fetchOps, metrics);
  }
}
