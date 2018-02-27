package com.spotify.modelserving.xgboost;

import com.spotify.modelserving.Model.PredictFn;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@FunctionalInterface
public interface XGBoostPredictFn<I, P> extends PredictFn<XGBoostModel<I>, I, float[], P> {

}
