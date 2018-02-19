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

import com.spotify.modelserving.IrisFeaturesSpec;
import org.junit.Assert;
import org.junit.Test;
import org.tensorflow.example.Example;
import scala.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class TensorFlowSavedModelTest {

  @Test
  public void testLoad() {
    // model downloaded locally:
    TensorFlowSavedModel model = TensorFlowSavedModel.from("/tmp/m_export/export/1518726944");

    InputStream r = this.getClass().getResourceAsStream("/iris.csv");

    Stream<IrisFeaturesSpec.Iris> irisStream = new BufferedReader(new InputStreamReader(r))
        .lines()
        .map(l -> l.split(","))
        .map(strs -> IrisFeaturesSpec.Iris$.MODULE$.apply(
            Option.apply(Double.parseDouble(strs[0])),
            Option.apply(Double.parseDouble(strs[1])),
            Option.apply(Double.parseDouble(strs[2])),
            Option.apply(Double.parseDouble(strs[3])),
            Option.apply(strs[4])));

    // hard coded settings (normally in gcs)
    String settings = "[{\"cls\":\"com.spotify.featran.transformers.StandardScaler\",\"name\":\"petal_length\",\"params\":{\"withStd\":\"true\",\"withMean\":\"true\"},\"featureNames\":[\"petal_length\"],\"aggregators\":\"3.7586666666666675,1.7585291834055217\"},{\"cls\":\"com.spotify.featran.transformers.StandardScaler\",\"name\":\"petal_width\",\"params\":{\"withStd\":\"true\",\"withMean\":\"true\"},\"featureNames\":[\"petal_width\"],\"aggregators\":\"1.1986666666666665,0.7606126185881715\"},{\"cls\":\"com.spotify.featran.transformers.StandardScaler\",\"name\":\"sepal_length\",\"params\":{\"withStd\":\"true\",\"withMean\":\"true\"},\"featureNames\":[\"sepal_length\"],\"aggregators\":\"5.843333333333332,0.8253012917851413\"},{\"cls\":\"com.spotify.featran.transformers.StandardScaler\",\"name\":\"sepal_width\",\"params\":{\"withStd\":\"true\",\"withMean\":\"true\"},\"featureNames\":[\"sepal_width\"],\"aggregators\":\"3.0540000000000007,0.43214658007054363\"},{\"cls\":\"com.spotify.featran.transformers.OneHotEncoder\",\"name\":\"class_name\",\"params\":{},\"featureNames\":[\"class_name_Iris-setosa\",\"class_name_Iris-versicolor\",\"class_name_Iris-virginica\"],\"aggregators\":\"label:Iris-setosa,label:Iris-versicolor,label:Iris-virginica\"}]";

    HashMap<String, Long> classToId = new HashMap<>(3);
    classToId.put("Iris-setosa", 0L);
    classToId.put("Iris-versicolor", 1L);
    classToId.put("Iris-virginica", 2L);


    int positivies = irisStream
        .map(i -> new HashMap.SimpleEntry<String, Example>(i.class_name().get(), model.extractFeatures(i, settings)))
        .mapToInt(i -> {
          long c = -1;
          try {
            c = model.predict(i.getValue(), 1L);
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
              e.printStackTrace();
          } catch (ExecutionException e) {
              e.printStackTrace();
          } catch (TimeoutException e) {
              e.printStackTrace();
          }
            return classToId.get(i.getKey()) == c ? 1 : 0;
        }).sum();

    Assert.assertTrue("Should be more the 0.8", positivies/150f > .8);
  }
}
