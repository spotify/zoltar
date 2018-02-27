package com.spotify.modelserving.featran;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.spotify.featran.java.JFeatureExtractor;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.transformers.MinMaxScaler;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class FeatranTest {
  @Test
  public void test() {
    List<Double> oldData = Lists.newArrayList(0.0, 1.0, 2.0);
    List<Double> newData = Lists.newArrayList(0.0, 0.5, 1.0);

    JFeatureSpec<Double> fs = JFeatureSpec.<Double>create()
            .required(x -> x, MinMaxScaler.apply("min-max", 0.0, 1.0));

    JFeatureExtractor<Double> fe1 = fs.extract(oldData);
    String settings = fe1.featureSettings();

    JFeatureExtractor<Double> fe2 = fs.extractWithSettings(newData, settings);
    List<Double> result = fe2.featureValuesDouble().stream()
            .map(a -> a[0])
            .collect(Collectors.toList());
    assertEquals(Lists.newArrayList(0.0, 0.25, 0.5), result);
  }
}
