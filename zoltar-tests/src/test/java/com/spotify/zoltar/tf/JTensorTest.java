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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.function.Function;
import org.junit.Test;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

public class JTensorTest {

  private static final String[] STRING_ARRAY_1DIMENSION = {"0", "1", "2"};
  private static final String[][][] STRING_ARRAY_3DIMENSIONS = {
      {{"000", "001", "002"},{"010", "011", "012"}, {"020", "021", "022"}},
      {{"100", "101", "102"},{"110", "111", "012"}, {"120", "121", "122"}},
      {{"200", "201", "202"},{"210", "211", "212"}, {"220", "221", "222"}}
  };
  private static final String[][][] STRING_ARRAY_3DIMENSIONS_ASCENDING = {
      {{"000", "001", "002"}, {"010", "011", "012"}}
  };
  private static final String[][][] STRING_ARRAY_3DIMENSIONS_DESCENDING = {
      {{"000"}, {"010"}}, {{"100"}, {"110"}}, {{"200"}, {"210"}}
  };

  private final long[] shape = {5L};

  @Test
  public void testStringTensor() {
    final String stringValue = "world";
    final Tensor<String> tensor = Tensors.create(stringValue);
    final JTensor jt = JTensor.create(tensor);
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
  public void testStringTensor1Dimension() {
    final byte[][] byteArray = toByteArray(STRING_ARRAY_1DIMENSION);
    final Tensor<String> tensor = Tensors.create(byteArray);
    final JTensor jt = JTensor.create(tensor);
    testMultidimensionalStringTensor(jt, STRING_ARRAY_1DIMENSION, new long[]{3});
  }

  @Test
  public void testStringTensor3Dimensions() {
    final byte[][][][] byteArray = toByteArray(STRING_ARRAY_3DIMENSIONS);
    final Tensor<String> tensor = Tensors.create(byteArray);
    final JTensor jt = JTensor.create(tensor);
    testMultidimensionalStringTensor(jt, STRING_ARRAY_3DIMENSIONS, new long[]{3, 3, 3});
  }

  @Test
  public void testStringTensor3DimensionsAscending() {
    final byte[][][][] byteArray = toByteArray(STRING_ARRAY_3DIMENSIONS_ASCENDING);
    final Tensor<String> tensor = Tensors.create(byteArray);
    final JTensor jt = JTensor.create(tensor);
    testMultidimensionalStringTensor(jt, STRING_ARRAY_3DIMENSIONS_ASCENDING, new long[]{1, 2, 3});
  }

  @Test
  public void testStringTensor3DimensionsDescending() {
    final byte[][][][] byteArray = toByteArray(STRING_ARRAY_3DIMENSIONS_DESCENDING);
    final Tensor<String> tensor = Tensors.create(byteArray);
    final JTensor jt = JTensor.create(tensor);
    testMultidimensionalStringTensor(jt, STRING_ARRAY_3DIMENSIONS_DESCENDING, new long[]{3, 2, 1});
  }

  @Test
  public void testIntTensor() {
    final int[] intValue = {1, 2, 3, 4, 5};
    final Tensor<Integer> tensor = Tensors.create(intValue);
    final JTensor jt = JTensor.create(tensor);
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
    final long[] longValue = {1, 2, 3, 4, 5};
    final Tensor<Long> tensor = Tensors.create(longValue);
    final JTensor jt = JTensor.create(tensor);
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
    final float[] floatValue = {1, 2, 3, 4, 5};
    final Tensor<Float> tensor = Tensors.create(floatValue);
    final JTensor jt = JTensor.create(tensor);
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
    final double[] doubleValue = {1, 2, 3, 4, 5};
    final Tensor<Double> tensor = Tensors.create(doubleValue);
    final JTensor jt = JTensor.create(tensor);
    assertEquals(DataType.DOUBLE, jt.dataType());
    assertEquals(1, jt.numDimensions());
    assertArrayEquals(shape, jt.shape());
    assertArrayEquals(doubleValue, jt.doubleValue(), 0.0);
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::floatValue);
  }

  @Test
  public void stringTensorSerializable() throws IOException {
    final String stringValue = "world";
    final Tensor<String> tensor = Tensors.create(stringValue);
    final JTensor jt = JTensor.create(tensor);

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @Test
  public void multidimensionalStringTensorSerializable() throws IOException {
    final byte[][][][] byteArray = toByteArray(STRING_ARRAY_3DIMENSIONS);
    final Tensor<String> tensor = Tensors.create(byteArray);
    final JTensor jt = JTensor.create(tensor);
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @Test
  public void intTensorSerializable() throws IOException {
    final int[] intValue = {1, 2, 3, 4, 5};
    final Tensor<Integer> tensor = Tensors.create(intValue);
    final JTensor jt = JTensor.create(tensor);

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @Test
  public void longTensorSerializable() throws IOException {
    final long[] longValue = {1, 2, 3, 4, 5};
    final Tensor<Long> tensor = Tensors.create(longValue);
    final JTensor jt = JTensor.create(tensor);

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @Test
  public void floatTensorSerializable() throws IOException {
    final float[] floatValue = {1, 2, 3, 4, 5};
    final Tensor<Float> tensor = Tensors.create(floatValue);
    final JTensor jt = JTensor.create(tensor);

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @Test
  public void doubleTensorSerializable() throws IOException {
    final double[] doubleValue = {1, 2, 3, 4, 5};
    final Tensor<Double> tensor = Tensors.create(doubleValue);
    final JTensor jt = JTensor.create(tensor);

    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(jt);
  }

  @SuppressWarnings("ReturnValueIgnored")
  private <T> void testException(final JTensor jt, final Function<JTensor, T> fn) {
    try {
      fn.apply(jt);
      throw new AssertionError("IllegalStateException expected, nothing thrown");
    } catch (final IllegalStateException e) {
      // expected, do nothing
    }
  }

  private void testMultidimensionalStringTensor(final JTensor jt, final Object[] expectedValue, final long[] expectedDimensions) {
    assertEquals(DataType.STRING, jt.dataType());
    assertEquals(expectedDimensions.length, jt.numDimensions());
    assertArrayEquals(expectedDimensions, jt.shape());
    assertArrayEquals(expectedValue, jt.value());
    testException(jt, JTensor::stringValue);
    testException(jt, JTensor::intValue);
    testException(jt, JTensor::longValue);
    testException(jt, JTensor::floatValue);
    testException(jt, JTensor::doubleValue);
  }

  private byte[][] toByteArray(final String[] stringValue) {
    return Arrays.stream(stringValue).map(item -> item.getBytes(UTF_8)).toArray(byte[][]::new);
  }

  private byte[][][][] toByteArray(final String[][][] stringValue) {
    return Arrays.stream(stringValue)
        .map(subArray1 -> Arrays.stream(subArray1)
            .map(subArray2 -> Arrays.stream(subArray2)
                .map(item -> item.getBytes(UTF_8))
                .toArray(byte[][]::new))
            .toArray(byte[][][]::new))
        .toArray(byte[][][][]::new);
  }
}
