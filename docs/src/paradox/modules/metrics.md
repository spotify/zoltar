# Metrics

`Zoltar` provides a way to attach and extend metrics around @javadoc[Predictor](com.spotify.zoltar.Predictor).

`zoltar-metrics` takes advantage of the [semantic-metrics](https://github.com/spotify/semantic-metrics)
to be able to capture meaningful metrics.

## Getting Started

Add `zoltar-metrics` dependency to your project:
 
@@dependency[Maven,Gradle,sbt] {
  group="com.spotify"
  artifact="zoltar-metrics"
  version="$project.version$"
}

Create a semantic registry:

@@snip [SemanticMetricRegistry](../../../../examples/custom-metrics/src/test/java/com/spotify/zoltar/examples/metrics/CustomMetricsExampleTest.java) { #SemanticMetricRegistry }

Attach it to the @javadoc[PredictorBuilder](com.spotify.zoltar.PredictorBuilder):

@@snip [PredictorMetrics](../../../../examples/custom-metrics/src/main/java/com/spotify/zoltar/examples/metrics/CustomMetricsExample.java) { #PredictorMetrics }

@@snip [PredictorWithMetrics](../../../../examples/custom-metrics/src/main/java/com/spotify/zoltar/examples/metrics/CustomMetricsExample.java) { #PredictorWithMetrics }

## Example 

Follow this @github[example](../../../../examples/custom-metrics) to see how you can create 
new set of metrics and attach them. 