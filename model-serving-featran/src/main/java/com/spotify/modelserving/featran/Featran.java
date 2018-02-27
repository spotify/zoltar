package com.spotify.modelserving.featran;

import static com.spotify.modelserving.fs.Resource.ReadFns.asString;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.DoubleSparseArray;
import com.spotify.featran.java.FloatSparseArray;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import com.spotify.modelserving.fs.Resource;
import java.io.IOException;
import java.net.URI;

public class Featran {

  public static <T> JRecordExtractor<T, float[]> loadFloatExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    String settings = Resource.from(path).read(asString());
    return JFeatureSpec.wrap(spec).extractWithSettingsFloat(settings);
  }

  public static <T> JRecordExtractor<T, double[]> loadDoubleExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    String settings = Resource.from(path).read(asString());
    return JFeatureSpec.wrap(spec).extractWithSettingsDouble(settings);
  }

  public static <T> JRecordExtractor<T, FloatSparseArray> loadFloatSparseArrayExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    String settings = Resource.from(path).read(asString());
    return JFeatureSpec.wrap(spec).extractWithSettingsFloatSparseArray(settings);
  }

  public static <T> JRecordExtractor<T, DoubleSparseArray> loadDoubleSparseArrayExtractor(
      FeatureSpec<T> spec, URI path) throws IOException {
    String settings = Resource.from(path).read(asString());
    return JFeatureSpec.wrap(spec).extractWithSettingsDoubleSparseArray(settings);
  }
}
