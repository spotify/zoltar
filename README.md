zoltar
======

[![Build Status](https://travis-ci.org/spotify/zoltar.svg?branch=master)](https://travis-ci.org/spotify/zoltar)
[![codecov.io](https://codecov.io/github/spotify/zoltar/coverage.svg?branch=master)](https://codecov.io/github/spotify/zoltar?branch=master)
[![Maven](https://img.shields.io/maven-central/v/com.spotify/zoltar-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Ccom.spotify.zoltar)
[![GitHub license](https://img.shields.io/github/license/spotify/zoltar.svg)](./LICENSE)

Common library for serving TensorFlow and XGBoost models in production.

Zoltar is a library that helps mount predictive machine learning models in [Apollo](https://github.com/spotify/apollo) and other Java services. It provides several key abstractions that allows you to featurize data, submit it to the model, and serve the prediction.



[Predictor](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/Predictor.java):
The core functionality of Zoltar. This object loads a model and calls functions to featurize input vectors and submit them for prediction. To these ends, a predictor is composed of a PredictFn, ModelLoader, and FeatureExtractor.

[PredictFn](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/PredictFns.java): 
An abstraction of a function that submits a feature vector to a model for prediction.

[Prediction](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/Prediction.java):
A wrapper around a single feature vector and its predicted output.

[ModelLoader](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/ModelLoader.java):
An object that loads an XGBoost or TensorFlow model from a filesystem.

[Model](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/Model.java):
The Java object that houses the predictive model itself.

[FeatureExtractor](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/FeatureExtractor.java):
Takes an input vector and applies a FeatureExtractFn to it.

[FeatureExtractFn](https://github.com/spotify/zoltar/blob/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-core/src/main/java/com/spotify/zoltar/FeatureExtractFns.java):
A function that converts a raw input vector and extracts a feature vector from it.


For more details, take a look at the source code and follow the documentation in the comments. We've created model specific implementations of these abstractions for TensorFlow and XGBoost, which can be found in [zoltar-tensorflow](https://github.com/spotify/zoltar/tree/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-tensorflow/src/main/java/com/spotify/zoltar/tf) and [zoltar-xgboost](https://github.com/spotify/zoltar/tree/6390c056d9e3d033bdaf1c2dedd4901e309571ec/zoltar-xgboost/src/main/java/com/spotify/zoltar/xgboost) respectively. We've also provided an [example Apollo service](https://github.com/spotify/zoltar/tree/6390c056d9e3d033bdaf1c2dedd4901e309571ec/examples/apollo-service-example/src/main/java/com/spotify/zoltar/examples/apollo) which demonstrates how these components are implemented and organized. We highly recommend you walk through this example before you try integrating Zoltar into your service.

# License

Copyright 2018 Spotify AB.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
