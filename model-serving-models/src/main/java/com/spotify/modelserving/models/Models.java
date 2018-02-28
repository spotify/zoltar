package com.spotify.modelserving.models;

import com.spotify.featran.FeatureSpec;
import com.spotify.modelserving.tf.TensorFlowModel;
import com.spotify.modelserving.xgboost.XGBoostModel;
import java.io.IOException;
import java.net.URI;

public final class Models {

  static <T> XGBoostModel<T> xgboost(final String modelUri,
                                     final String settingsUri,
                                     final FeatureSpec<T> featureSpec) throws IOException {
    return XGBoostModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                           final String settingsUri,
                                           final FeatureSpec<T> featureSpec) throws IOException {
    return TensorFlowModel.create(URI.create(modelUri), URI.create(settingsUri), featureSpec);
  }

  static <T> TensorFlowModel<T> tensorFlow(final String modelUri,
                                           final String settingsUri,
                                           final FeatureSpec<T> featureSpec,
                                           final TensorFlowModel.Options options)
      throws IOException {
    return TensorFlowModel
        .create(URI.create(modelUri), URI.create(settingsUri), featureSpec, options);
  }
}
