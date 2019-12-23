/*
 * Copyright (C) 2019 Spotify AB
 *
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
 */
package com.spotify.zoltar.tf;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.tensorflow.DataType;
import org.tensorflow.Tensor;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/** Wrapper for {@link Tensor} that manages memory in JVM heap and closes the underlying Tensor. */
@AutoValue
public abstract class JTensor implements Serializable {
  /**
   * Create a new {@link JTensor} instance by extracting data from the underlying {@link Tensor} and
   * closing it afterwards.
   */
  public static JTensor create(final Tensor<?> tensor) {
    final JTensor jt;
    try {
      switch (tensor.dataType()) {
        case STRING:
          if (tensor.numDimensions() == 0) {
            final String value = new String(tensor.bytesValue(), UTF_8);
            jt =
                new AutoValue_JTensor(
                    tensor.dataType(), tensor.numDimensions(), tensor.shape(), value);
          } else {
            final int[] dimensions = toIntExact(tensor.shape());
            final Object byteArray =
                tensor.copyTo(Array.newInstance(byte[].class, toIntExact(tensor.shape())));
            jt =
                new AutoValue_JTensor(
                    tensor.dataType(),
                    tensor.numDimensions(),
                    tensor.shape(),
                    toStringArray(byteArray, tensor.numElements(), dimensions));
          }
          break;
        case INT32:
          final IntBuffer intBuf = IntBuffer.allocate(tensor.numElements());
          tensor.writeTo(intBuf);
          jt =
              new AutoValue_JTensor(
                  tensor.dataType(), tensor.numDimensions(), tensor.shape(), intBuf.array());
          break;
        case INT64:
          final LongBuffer longBuf = LongBuffer.allocate(tensor.numElements());
          tensor.writeTo(longBuf);
          jt =
              new AutoValue_JTensor(
                  tensor.dataType(), tensor.numDimensions(), tensor.shape(), longBuf.array());
          break;
        case FLOAT:
          final FloatBuffer floatBuf = FloatBuffer.allocate(tensor.numElements());
          tensor.writeTo(floatBuf);
          jt =
              new AutoValue_JTensor(
                  tensor.dataType(), tensor.numDimensions(), tensor.shape(), floatBuf.array());
          break;
        case DOUBLE:
          final DoubleBuffer doubleBuf = DoubleBuffer.allocate(tensor.numElements());
          tensor.writeTo(doubleBuf);
          jt =
              new AutoValue_JTensor(
                  tensor.dataType(), tensor.numDimensions(), tensor.shape(), doubleBuf.array());
          break;
        case BOOL:
          final boolean[] array = new boolean[tensor.numElements()];
          tensor.copyTo(array);
          jt =
              new AutoValue_JTensor(
                  tensor.dataType(), tensor.numDimensions(), tensor.shape(), array);
          break;
        default:
          throw new IllegalStateException("Unsupported data type " + tensor.dataType());
      }
    } finally {
      tensor.close();
    }

    return jt;
  }

  /** {@link DataType} of the underlying {@link Tensor}. */
  public abstract DataType dataType();

  /** Number of dimensions of the underlying {@link Tensor}. */
  abstract int numDimensions();

  /** Shape of the underlying {@link Tensor}. */
  abstract long[] shape();

  protected abstract Object data();

  /** Value of the underlying {@link Tensor}, lets the caller take care of typing. */
  @SuppressWarnings("unchecked")
  public <T> T value() {
    return (T) data();
  }

  /**
   * String value of the underlying {@link Tensor}, if {@link DataType} is {@code STRING} and {@link
   * #numDimensions()} is 0.
   */
  public String stringValue() {
    Preconditions.checkState(dataType() == DataType.STRING);
    Preconditions.checkState(this.numDimensions() == 0);
    return (String) data();
  }

  /** Integer array value of the underlying {@link Tensor}, if {@link DataType} is {@code INT32}. */
  public int[] intValue() {
    Preconditions.checkState(dataType() == DataType.INT32);
    return (int[]) data();
  }

  /** Long array value of the underlying {@link Tensor}, if {@link DataType} is {@code INT64}. */
  public long[] longValue() {
    Preconditions.checkState(dataType() == DataType.INT64);
    return (long[]) data();
  }

  /** Float array value of the underlying {@link Tensor}, if {@link DataType} is {@code FLOAT}. */
  public float[] floatValue() {
    Preconditions.checkState(dataType() == DataType.FLOAT);
    return (float[]) data();
  }

  /** Double array value of the underlying {@link Tensor}, if {@link DataType} is {@code DOUBLE}. */
  public double[] doubleValue() {
    Preconditions.checkState(dataType() == DataType.DOUBLE);
    return (double[]) data();
  }

  public boolean[] booleanValue() {
    Preconditions.checkState(dataType() == DataType.BOOL);
    return (boolean[]) data();
  }

  private static int[] toIntExact(final long[] dimensions) {
    final int[] intDimensions = new int[dimensions.length];
    for (int i = 0; i < dimensions.length; i++) {
      intDimensions[i] = Math.toIntExact(dimensions[i]);
    }
    return intDimensions;
  }

  private static Object toStringArray(
      final Object byteArray, final int numElements, final int... dimensions) {
    final int numDimensions = dimensions.length;
    final Object stringArray = Array.newInstance(String.class, dimensions);
    final int[] currentIndexes = new int[numDimensions];

    // iterate all elements
    for (int n = 0; n < numElements; n++) {

      // make currentIndexes point to the element we are populating in a multidimensional array
      int quotient = n;
      for (int d = numDimensions - 1; d >= 0; d--) {
        currentIndexes[d] = quotient % dimensions[d];
        quotient = quotient / dimensions[d];
      }

      // walk down the input array to select the corresponding byte[]
      Object currentSubByteArray = byteArray;
      for (int i = 0; i < numDimensions; i++) {
        currentSubByteArray = Array.get(currentSubByteArray, currentIndexes[i]);
      }

      // walk down the output array to select parent array of current position so we can set
      final String value = new String((byte[]) currentSubByteArray, UTF_8);
      Object currentSubStringArray = stringArray;
      for (int i = 0; i < numDimensions - 1; i++) {
        currentSubStringArray = Array.get(currentSubStringArray, currentIndexes[i]);
      }

      // set the value
      Array.set(currentSubStringArray, currentIndexes[numDimensions - 1], value);
    }

    return stringArray;
  }
}
