package com.spotify.modelserving.xgboost;

import static com.spotify.modelserving.fs.Resource.ReadFns.asString;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.Model;
import com.spotify.modelserving.fs.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.GompLoader;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class XGBoostModel<T> implements Model<Booster, T> {

  private final Booster booster;
  private final String settings;
  private final JFeatureSpec<T> featureSpec;

  private XGBoostModel(Booster booster,
                       String settings,
                       JFeatureSpec<T> featureSpec) {
    this.booster = booster;
    this.settings = settings;
    this.featureSpec = featureSpec;
  }

  public static <T> XGBoostModel<T> create(String modelUri,
                                           String settingsUri,
                                           FeatureSpec<T> featureSpec) throws IOException {
    return create(URI.create(modelUri), URI.create(settingsUri), JFeatureSpec.wrap(featureSpec));
  }

  public static <T> XGBoostModel<T> create(URI modelUri,
                                           URI settingsUri,
                                           FeatureSpec<T> featureSpec) throws IOException {
    return create(modelUri, settingsUri, JFeatureSpec.wrap(featureSpec));
  }

  public static <T> XGBoostModel<T> create(String modelUri,
                                           String settingsUri,
                                           JFeatureSpec<T> featureSpec) throws IOException {
    return create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  public static <T> XGBoostModel<T> create(URI modelUri,
                                           URI settingsUri,
                                           JFeatureSpec<T> featureSpec) throws IOException {
    try {
      GompLoader.start();
      final InputStream is = Resource.from(modelUri).open();
      final String settings = Resource.from(settingsUri).read(asString());
      return new XGBoostModel<>(XGBoost.loadModel(is), settings, featureSpec);
    } catch (XGBoostError xgBoostError) {
      throw new IOException(xgBoostError);
    }
  }

  @Override
  public Booster instance() {
    return booster;
  }

  @Override
  public String settings() {
    return settings;
  }

  @Override
  public JFeatureSpec<T> featureSpec() {
    return featureSpec;
  }

  @Override
  public void close() throws Exception {

  }
}
