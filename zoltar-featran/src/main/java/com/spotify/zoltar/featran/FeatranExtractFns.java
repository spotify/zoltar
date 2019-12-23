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
package com.spotify.zoltar.featran;

import ml.dmlc.xgboost4j.LabeledPoint;

import org.tensorflow.example.Example;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.DoubleSparseArray;
import com.spotify.featran.java.FloatSparseArray;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.xgboost.SparseLabeledPoint;
import com.spotify.zoltar.FeatureExtractFns.ExtractFn;
import com.spotify.zoltar.FeatureExtractor;

/**
 * Featran extraction functions. Functions used to transform raw input into extracted features,
 * should be used together with {@link FeatureExtractor}.
 *
 * @see FeatureExtractor
 */
public final class FeatranExtractFns {

  private FeatranExtractFns() {}

  /**
   * Extract features as {@code double[]}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, double[]> doubles(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, double[]> doubles(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsDouble(settings)::featureValue);
  }

  /**
   * Extract features as {@link Example}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, Example> example(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, Example> example(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsExample(settings)::featureValue);
  }

  /**
   * Extract features as {@code float[]}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, float[]> floats(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, float[]> floats(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsFloat(settings)::featureValue);
  }

  /**
   * Extract features as {@link FloatSparseArray}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, FloatSparseArray> sparseFloats(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, FloatSparseArray> sparseFloats(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsFloatSparseArray(settings)::featureValue);
  }

  /**
   * Extract features as {@link DoubleSparseArray}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, DoubleSparseArray> sparseDoubles(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, DoubleSparseArray> sparseDoubles(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsDoubleSparseArray(settings)::featureValue);
  }

  /**
   * Extract features as {@link LabeledPoint}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, LabeledPoint> labeledPoints(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, LabeledPoint> labeledPoints(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(featureSpec.extractWithSettingsLabeledPoint(settings)::featureValue);
  }

  /**
   * Extract features as {@link SparseLabeledPoint}.
   *
   * @param featureSpec Featran's {@link FeatureSpec}.
   * @param settings JSON settings from a previous session.
   * @param <InputT> type of the input to feature extraction.
   * @return feature extraction result.
   */
  public static <InputT> ExtractFn<InputT, SparseLabeledPoint> sparseLabeledPoints(
      final FeatureSpec<InputT> featureSpec, final String settings) {
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
  public static <InputT> ExtractFn<InputT, SparseLabeledPoint> sparseLabeledPoints(
      final JFeatureSpec<InputT> featureSpec, final String settings) {
    return ExtractFn.lift(
        featureSpec.extractWithSettingsSparseLabeledPoint(settings)::featureValue);
  }
}
