package com.spotify.modelserving.featran;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.DoubleSparseArray;
import com.spotify.featran.java.FloatSparseArray;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.featran.java.JRecordExtractor;
import com.spotify.modelserving.fs.FileSystems;

import java.io.IOException;

public class Featran {

  public static <T> JRecordExtractor<T, float[]> loadFloatExtractor(
          FeatureSpec<T> spec, String path) throws IOException {
    String settings = FileSystems.readString(path);
    return JFeatureSpec.wrap(spec).extractWithSettingsFloat(settings);
  }

  public static <T> JRecordExtractor<T, double[]> loadDoubleExtractor(
          FeatureSpec<T> spec, String path) throws IOException {
    String settings = FileSystems.readString(path);
    return JFeatureSpec.wrap(spec).extractWithSettingsDouble(settings);
  }

  public static <T> JRecordExtractor<T, FloatSparseArray> loadFloaSparseArrayExtractor(
          FeatureSpec<T> spec, String path) throws IOException {
    String settings = FileSystems.readString(path);
    return JFeatureSpec.wrap(spec).extractWithSettingsFloatSparseArray(settings);
  }

  public static <T> JRecordExtractor<T, DoubleSparseArray> loadDoubleSparseArrayExtractor(
          FeatureSpec<T> spec, String path) throws IOException {
    String settings = FileSystems.readString(path);
    return JFeatureSpec.wrap(spec).extractWithSettingsDoubleSparseArray(settings);
  }
}
