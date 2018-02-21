package com.spotify.modelserving.xgboost;

import com.spotify.modelserving.Model;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import ml.dmlc.xgboost4j.LabeledPoint;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class XGBoostModel implements Model {

  private final Booster booster;

  private XGBoostModel(Booster booster) {
    this.booster = booster;
  }

  public static XGBoostModel load(InputStream inputStream) throws Exception {
    return new XGBoostModel(XGBoost.loadModel(inputStream));
  }


  public float[][] predict(LabeledPoint... labeledPoints) throws Exception {
    final Iterator<LabeledPoint> iterator = Arrays.asList(labeledPoints).iterator();
    final DMatrix dMatrix = new DMatrix(iterator, null);

    return booster.predict(dMatrix);
  }

}
