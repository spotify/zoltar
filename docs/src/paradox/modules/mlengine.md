# Google Cloud ML Engine

## Getting started

To use `Google Cloud ML Engine`, you need to deploy your model first! 
Deployment might depend on the type of model you are using:

* [TensorFlow](https://cloud.google.com/ml-engine/docs/tensorflow/deploying-models).
* [scikit-learn](https://cloud.google.com/ml-engine/docs/scikit/quickstart#deploy_models_and_versions) or [XGBoost](https://cloud.google.com/ml-engine/docs/scikit/quickstart#deploy_models_and_versions). 

## Usage

Include the following dependency:

@@dependency[Maven,Gradle,sbt] {
  group="com.spotify"
  artifact="zoltar-mlengine"
  version="$project.version$"
}

Replace your existing @javadoc[ModelLoader](com.spotify.zoltar.core.ModelLoader) with 
@javadoc[MlEngineLoader](com.spotify.zoltar.mlengine.MlEngineLoader)

@@snip [MlEnginePredictorExample.java](../../../../examples/mlengine-example/src/main/java/com/spotify/zoltar/examples/mlengine/MlEnginePredictorExample.java) { #MlEngineLoader }

## Example

Follow this @github[example](../../../../examples/mlengine-example)
to create a @javadoc[Predictor](com.spotify.zoltar.core.Predictor) that uses `Google Cloud ML Engine` deployed models.