package com.spotify.modelserving.tf;

import com.spotify.modelserving.Model.PredictFn;
import org.tensorflow.example.Example;

@FunctionalInterface
public interface TensorFlowPredictFn<I, P> extends PredictFn<TensorFlowModel<I>, I, Example, P> {

}
