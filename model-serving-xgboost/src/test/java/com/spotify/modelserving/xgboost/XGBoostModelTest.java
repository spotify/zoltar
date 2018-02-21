package com.spotify.modelserving.xgboost;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.IrisFeaturesSpec;
import com.spotify.modelserving.IrisFeaturesSpec.Iris;
import com.spotify.modelserving.fs.FileSystems;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ml.dmlc.xgboost4j.LabeledPoint;
import org.junit.Test;
import scala.Option;

public class XGBoostModelTest {

  @Test
  public void testLoadingModel() throws Exception {
    final InputStream model =
        FileSystems.open("resource:///iris.model");

    XGBoostModel.load(model);
  }

  public Stream<LabeledPoint> extractVectors(IrisFeaturesSpec.Iris input, String settings) {
    // The line below will be red because Iris class is altered by macro
    FeatureSpec<Iris> featuresSpec = IrisFeaturesSpec.irisFeaturesSpec();
    return JFeatureSpec.wrap(featuresSpec)
        .extractWithSettings(Collections.singletonList(input), settings)
        .featureValuesFloat()
        .stream()
        .map(vec -> new LabeledPoint(0, null, vec));
  }

  @Test
  public void testModelPrediction() throws Exception {
    final InputStream modelInput =
        FileSystems.open("resource:///iris.model");
    final InputStream testInput =
        FileSystems.open("resource:///iris.csv");
    final String settings =
        FileSystems.readString("resource:///settings.json");

    // Iris$ will be red because it's macro generated, and intellij seems to have
    // hard time figuring out java/scala order with macros.
    List<Iris> irisStream =
        new BufferedReader(new InputStreamReader(testInput))
            .lines()
            .map(l -> l.split(","))
            .map(strs -> IrisFeaturesSpec.Iris$.MODULE$.apply(
                Option.apply(Double.parseDouble(strs[0])),
                Option.apply(Double.parseDouble(strs[1])),
                Option.apply(Double.parseDouble(strs[2])),
                Option.apply(Double.parseDouble(strs[3])),
                Option.apply(strs[4])))
            .collect(Collectors.toList());

    XGBoostModel model = XGBoostModel.load(modelInput);

    Map<Integer, String> classToId = ImmutableMap.of(
        0, "Iris-setosa",
        1, "Iris-versicolor",
        2, "Iris-virginica");

    int sum = irisStream.stream().mapToInt(input -> {
      String className = input.class_name().get();
      LabeledPoint[] labeledPoints =
          extractVectors(input, settings).toArray(LabeledPoint[]::new);

      try {
        float[] score = model.predict(labeledPoints)[0];
        int idx = IntStream.range(0, score.length)
            .reduce((i, j) -> score[i] >= score[j] ? i : j)
            .getAsInt();
        return classToId.get(idx).equals(className) ? 1 : 0;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).sum();

    assertTrue("Should be more the 0.8", sum / (float) irisStream.size() > .8);
  }
}