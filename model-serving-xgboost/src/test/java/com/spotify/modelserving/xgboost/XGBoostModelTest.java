package com.spotify.modelserving.xgboost;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.modelserving.IrisFeaturesSpec;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.Model.FeatureExtractFn;
import com.spotify.modelserving.Model.Prediction;
import com.spotify.modelserving.Model.Predictor;
import com.spotify.modelserving.fs.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ml.dmlc.xgboost4j.LabeledPoint;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.Test;
import scala.Option;

public class XGBoostModelTest {

  @Test
  public void testLoadingModel() throws Exception {
    final URI trainData = URI.create("resource:///iris.model");
    final URI settings = URI.create("resource:///settings.json");
    final FeatureSpec<Iris> featuresSpec = IrisFeaturesSpec.irisFeaturesSpec();

    XGBoostModel.create(trainData, settings, featuresSpec);
  }

  @Test
  public void testModelPrediction() throws Exception {
    final List<Iris> irisStream = Resource.from("resource:///iris.csv").read(is -> {
      // Iris$ will be red because it's macro generated, and intellij seems to have
      // hard time figuring out java/scala order with macros.
      return new BufferedReader(new InputStreamReader(is.open()))
          .lines()
          .map(l -> l.split(","))
          .map(strs -> (Iris) IrisFeaturesSpec.Iris$.MODULE$.apply(
              Option.apply(Double.parseDouble(strs[0])),
              Option.apply(Double.parseDouble(strs[1])),
              Option.apply(Double.parseDouble(strs[2])),
              Option.apply(Double.parseDouble(strs[3])),
              Option.apply(strs[4])))
          .collect(Collectors.toList());
    });

    Map<Integer, String> classToId = ImmutableMap.of(0, "Iris-setosa",
                                                     1, "Iris-versicolor",
                                                     2, "Iris-virginica");

    XGBoostPredictFn<Iris, float[]> predictFn = (model, vectors) -> {
      return vectors.stream().map(vector -> {
        LabeledPoint labeledPoints = new LabeledPoint(0, null, vector.value());
        try {
          final Iterator<LabeledPoint> iterator =
              Collections.singletonList(labeledPoints).iterator();
          final DMatrix dMatrix = new DMatrix(iterator, null);

          return Prediction.create(vector.input(), model.instance().predict(dMatrix)[0]);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList());
    };

    XGBoostModel<Iris> model = XGBoostModel.create("resource:///iris.model",
                                                   "resource:///settings.json",
                                                   IrisFeaturesSpec.irisFeaturesSpec());
    FeatureExtractFn<Iris, float[]> featureExtractFn = JFeatureExtractor::featureValuesFloat;

    IntStream predictions = Predictor
        .create(model, featureExtractFn, predictFn)
        .predict(irisStream)
        .stream()
        .mapToInt(prediction -> {
          String className = prediction.input().class_name().get();
          float[] score = prediction.value();
          int idx = IntStream.range(0, score.length)
              .reduce((i, j) -> score[i] >= score[j] ? i : j)
              .getAsInt();

          return classToId.get(idx).equals(className) ? 1 : 0;
        });

    assertTrue("Should be more the 0.8", predictions.sum() / (float) irisStream.size() > .8);
  }
}