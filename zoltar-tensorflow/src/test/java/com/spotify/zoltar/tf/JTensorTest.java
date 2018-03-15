/*-
 * -\-\-
 * zoltar-tensorflow
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.zoltar.tf;

import org.junit.Test;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JTensorTest {

  private final long[] shape = {5L};

  @Test
  public void testStringTensor() {
    String stringValue = "world";
    Tensor<String> tensor = Tensors.create(stringValue);
    JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.STRING, jt.dataType());
    assertEquals(0, jt.numDimensions());
    assertArrayEquals(new long[0], jt.shape());
    assertEquals(stringValue, jt.stringValue());
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::floatValue);
    testException(jt, JTensor::doubleValue);
  }

  @Test
  public void testIntTensor() {
    int[] intValue = {1, 2, 3, 4, 5};
    Tensor<Integer> tensor = Tensors.create(intValue);
    JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.INT32, jt.dataType());
    assertEquals(1, jt.numDimensions());
    assertArrayEquals(shape, jt.shape());
    assertArrayEquals(intValue, jt.intValue());
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::floatValue);
    testException(jt, JTensor::doubleValue);
  }

  @Test
  public void testLongTensor() {
    long[] longValue = {1, 2, 3, 4, 5};
    Tensor<Long> tensor = Tensors.create(longValue);
    JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.INT64, jt.dataType());
    assertEquals(1, jt.numDimensions());
    assertArrayEquals(shape, jt.shape());
    assertArrayEquals(longValue, jt.longValue());
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::floatValue);
    testException(jt, JTensor::doubleValue);
  }

  @Test
  public void testFloatTensor() {
    float[] floatValue = {1, 2, 3, 4, 5};
    Tensor<Float> tensor = Tensors.create(floatValue);
    JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.FLOAT, jt.dataType());
    assertEquals(1, jt.numDimensions());
    assertArrayEquals(shape, jt.shape());
    assertArrayEquals(floatValue, jt.floatValue(), 0.0f);
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::doubleValue);
  }

  @Test
  public void testDoubleTensor() {
    double[] doubleValue = {1, 2, 3, 4, 5};
    Tensor<Double> tensor = Tensors.create(doubleValue);
    JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.DOUBLE, jt.dataType());
    assertEquals(1, jt.numDimensions());
    assertArrayEquals(shape, jt.shape());
    assertArrayEquals(doubleValue, jt.doubleValue(), 0.0);
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::floatValue);
  }

  private <T> void testException(JTensor jt, Function<JTensor, T> fn) {
    try {
      fn.apply(jt);
      throw new AssertionError("IllegalStateException expected, nothing thrown");
    } catch (IllegalStateException e) {
      // expected, do nothing
    }
  }
}
