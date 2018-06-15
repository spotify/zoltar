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

package com.spotify.zoltar.featran;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.DoubleSparseArray;
import com.spotify.featran.java.FloatSparseArray;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.xgboost.SparseLabeledPoint;
import com.spotify.zoltar.FeatureExtractFns.BatchExtractFn;
import com.spotify.zoltar.FeatureExtractFns.SingleExtractFn;
import com.spotify.zoltar.FeatureExtractor;
import ml.dmlc.xgboost4j.LabeledPoint;
import org.tensorflow.example.Example;

/**
 * Featran extraction functions. Functions used to transform raw input into extracted features,
 * should be used together with {@link FeatureExtractor}.
 *
 * @see FeatureExtractor
 */
public final class FeatranExtractFns {

  private FeatranExtractFns() {
  }

  /**
   * Extract features as {@code double[]}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, double[]> doubles(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return doubles(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@code double[]}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, double[]> doubles(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsDouble(settings)::featureValue;
  }

  /**
   * Extract features as {@link Example}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, Example> example(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return example(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@link Example}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, Example> example(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsExample(settings)::featureValue;
  }

  /**
   * Extract features as a batch of {@link Example}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings    JSON settings from a previous session.
   * @param <InputT>    type of the input to feature extraction.
   *
   * @return feature extraction result.
   */
  public static <InputT> BatchExtractFn<InputT, Example> exampleBatch(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return exampleBatch(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as a batch of {@link Example}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings    JSON settings from a previous session.
   * @param <InputT>    type of the input to feature extraction.
   *
   * @return feature extraction result.
   */
  public static <InputT> BatchExtractFn<InputT, Example> exampleBatch(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsExample(settings)::featureValue;
  }

  /**
   * Extract features as {@code float[]}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, float[]> floats(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return floats(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@code float[]}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, float[]> floats(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsFloat(settings)::featureValue;
  }

  /**
   * Extract features as {@link FloatSparseArray}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, FloatSparseArray> sparseFloats(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return sparseFloats(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@link FloatSparseArray}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, FloatSparseArray> sparseFloats(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsFloatSparseArray(settings)::featureValue;
  }

  /**
   * Extract features as {@link DoubleSparseArray}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, DoubleSparseArray> sparseDoubles(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return sparseDoubles(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@link DoubleSparseArray}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, DoubleSparseArray> sparseDoubles(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsDoubleSparseArray(settings)::featureValue;
  }

  /**
   * Extract features as {@link LabeledPoint}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, LabeledPoint> labeledPoints(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return labeledPoints(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@link LabeledPoint}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, LabeledPoint> labeledPoints(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsLabeledPoint(settings)::featureValue;
  }

  /**
   * Extract features as {@link SparseLabeledPoint}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, SparseLabeledPoint> sparseLabeledPoints(
      final FeatureSpec<InputT> featureSpec,
      final String settings) {
    return sparseLabeledPoints(JFeatureSpec.wrap(featureSpec), settings);
  }

  /**
   * Extract features as {@link SparseLabeledPoint}.
   *
   * @param featureSpec Featran's {@link JFeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> SingleExtractFn<InputT, SparseLabeledPoint> sparseLabeledPoints(
      final JFeatureSpec<InputT> featureSpec,
      final String settings) {
    return featureSpec.extractWithSettingsSparseLabeledPoint(settings)::featureValue;
  }

}
