# Zoltar

[![Build Status](https://travis-ci.org/spotify/zoltar.svg?branch=master)](https://travis-ci.org/spotify/zoltar)
[![codecov.io](https://codecov.io/github/spotify/zoltar/coverage.svg?branch=master)](https://codecov.io/github/spotify/zoltar?branch=master)
[![Maven](https://img.shields.io/maven-central/v/com.spotify/zoltar-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Ccom.spotify.zoltar)
[![GitHub license](https://img.shields.io/github/license/spotify/zoltar.svg)](./LICENSE)

Common library for serving TensorFlow and XGBoost models in production.

Zoltar is a library that helps load predictive machine learning models in a JVM. It provides several key abstractions which you can implement to help your service load a serialized model, featurize input data, submit feature vectors to the model, and serve the model's predictions. Below is a quick overview of these abstractions which can be found in @github[zoltar-core](../../zoltar-core/src/main/java/com/spotify/zoltar).

## Features

* TensorFlow model loading and prediction.
* XGBoost model loading and prediction.
* Filesystems:
    * Local
    * Google Cloud Storage
* [Featran](https://github.com/spotify/featran) featurization library integration.
* Easy integration with [Apollo](https://github.com/spotify/apollo) services.

## Abstractions

@javadoc[Predictor](com.spotify.zoltar.Predictor):
The core functionality of Zoltar. This object loads a model and calls functions to featurize input vectors and submit them for prediction. To these ends, a predictor is composed of a ModelLoader, FeatureExtractor, and PredictFn.

@javadoc[PredictFn](com.spotify.zoltar.PredictFns):
A function that submits a feature vector to a model for prediction.

@javadoc[Prediction](com.spotify.zoltar.Prediction):
A wrapper around a single feature vector and its predicted output.

@javadoc[ModelLoader](com.spotify.zoltar.ModelLoader):
An object that loads an XGBoost or TensorFlow model from a supported filesystem. 

@javadoc[Model](com.spotify.zoltar.Model):
The Java object that houses the predictive model itself.

@javadoc[FeatureExtractor](com.spotify.zoltar.FeatureExtractor):
Takes an input vector and applies a FeatureExtractFn to it.

@javadoc[FeatureExtractFn](com.spotify.zoltar.FeatureExtractFns):
A function that takes a raw input vector and extracts a feature vector from it.

For more details, take a look at the source code and follow the documentation in the comments. If you're using TensorFlow or XGBoost, you can find model specific implementations of these abstractions in @javadoc[zoltar-tensorflow](com.spotify.zoltar.tf.package-summary) and @javadoc[zoltar-xgboost](com.spotify.zoltar.xgboost.package-summary) respectively.

We've also provided an @github[example](../../examples/apollo-service-example/src/main/java/com/spotify/zoltar/examples/apollo) service built on the [Apollo](https://github.com/spotify/apollo) framework. It demonstrates how Zoltar components are implemented and organized. We highly recommend you walk through this example before you try integrating Zoltar into your applications.

@@@index
* [Getting Started](getting-started.md)
* [Modules](modules/index.md)
* [Release](release.md)
@@@