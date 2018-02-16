/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving.tf;

import com.spotify.featran.FeatureSpec;
import com.spotify.featran.java.JFeatureSpec;
import com.spotify.modelserving.IrisFeaturesSpec;
import com.spotify.modelserving.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.tensorflow.example.Example;
import org.tensorflow.example.Feature;
import org.tensorflow.example.Features;
import org.tensorflow.example.FloatList;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TensorFlowSavedModel implements Model, AutoCloseable {

  private SavedModelBundle model = null;

  public static TensorFlowSavedModel from(String exportDir) {
    return new TensorFlowSavedModel(exportDir);
  }

  private TensorFlowSavedModel(String exportDir) {
    // TODO: copy saved model from remote FS, object stores etc to local filesystem
    // tags come from: https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/saved_model/tag_constants.py#L26
    model = SavedModelBundle.load(exportDir, "serve");
  }

  public Example extractFeatures(IrisFeaturesSpec.Iris input, String settings) {
    FeatureSpec<IrisFeaturesSpec.Iris> irisFeatureSpec = IrisFeaturesSpec.irisFeaturesSpec();
    return JFeatureSpec.wrap(irisFeatureSpec)
        .extractWithSettings(Collections.singletonList(input), settings)
        .featureValuesExample().get(0);
  }

  public long predict(Example example, long timeoutSeconds) throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // rank 1 cause we need to account for batch
    byte[][] b = new byte[1][];
    b[0] = example.toByteArray();
    try (
        Tensor<String> t = Tensors.create(b);
    ) {
      Session.Runner runner = model.session().runner()
          .feed("input_example_tensor", t)
          .fetch("linear/head/predictions/class_ids");

      List<Tensor<?>> output = CompletableFuture
              .supplyAsync(runner::run).get(timeoutSeconds, TimeUnit.SECONDS);


      LongBuffer incomingClassId = LongBuffer.allocate(1);
      try {
        output.get(0).writeTo(incomingClassId);
      } finally {
        output.forEach(Tensor::close);
      }
      return incomingClassId.get(0);
    }
  }

  @Override
  public void close() throws Exception {
    if (model != null) {
      model.close();
    }
  }
}
