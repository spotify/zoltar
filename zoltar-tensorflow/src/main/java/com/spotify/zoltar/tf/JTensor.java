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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;

/**
 * Wrapper for {@link Tensor} that manages memory in JVM heap and closes the underlying Tensor.
 */
@AutoValue
public abstract class JTensor implements Serializable {
  /**
   * Create a new {@link JTensor} instance by extracting data from the underlying {@link Tensor}
   * and closing it afterwards.
   */
  public static JTensor create(final Tensor<?> tensor) {
    final JTensor jt;

    switch (tensor.dataType()) {
      case STRING:
        final String value = new String(tensor.bytesValue());
        jt = new AutoValue_JTensor(
                tensor.dataType(), tensor.numDimensions(), tensor.shape(), value);
        break;
      case INT32:
        final IntBuffer intBuf = IntBuffer.allocate(tensor.numElements());
        tensor.writeTo(intBuf);
        jt = new AutoValue_JTensor(
                tensor.dataType(), tensor.numDimensions(), tensor.shape(), intBuf.array());
        break;
      case INT64:
        final LongBuffer longBuf = LongBuffer.allocate(tensor.numElements());
        tensor.writeTo(longBuf);
        jt = new AutoValue_JTensor(
                tensor.dataType(), tensor.numDimensions(), tensor.shape(), longBuf.array());
        break;
      case FLOAT:
        final FloatBuffer floatBuf = FloatBuffer.allocate(tensor.numElements());
        tensor.writeTo(floatBuf);
        jt = new AutoValue_JTensor(
                tensor.dataType(), tensor.numDimensions(), tensor.shape(), floatBuf.array());
        break;
      case DOUBLE:
        final DoubleBuffer doubleBuf = DoubleBuffer.allocate(tensor.numElements());
        tensor.writeTo(doubleBuf);
        jt = new AutoValue_JTensor(
                tensor.dataType(), tensor.numDimensions(), tensor.shape(), doubleBuf.array());
        break;
      default:
        tensor.close();
        throw new IllegalStateException("Unsupported data type " + tensor.dataType());
    }

    tensor.close();
    return jt;
  }

  /**
   * {@link DataType} of the underlying {@link Tensor}.
   */
  abstract DataType dataType();

  /**
   * Number of dimensions of the underlying {@link Tensor}.
   */
  abstract int numDimensions();

  /**
   * Shape of the underlying {@link Tensor}.
   */
  abstract long[] shape();

  protected abstract Object data();

  /**
   * String value of the underlying {@link Tensor}, if {@link DataType} is {@code STRING}.
   */
  public String stringValue() {
    Preconditions.checkState(dataType() == DataType.STRING);
    return (String) data();
  }

  /**
   * Integer array value of the underlying {@link Tensor}, if {@link DataType} is {@code INT32}.
   */
  public int[] intValue() {
    Preconditions.checkState(dataType() == DataType.INT32);
    return (int[]) data();
  }

  /**
   * Long array value of the underlying {@link Tensor}, if {@link DataType} is {@code INT64}.
   */
  public long[] longValue() {
    Preconditions.checkState(dataType() == DataType.INT64);
    return (long[]) data();
  }

  /**
   * Float array value of the underlying {@link Tensor}, if {@link DataType} is {@code FLOAT}.
   */
  public float[] floatValue() {
    Preconditions.checkState(dataType() == DataType.FLOAT);
    return (float[]) data();
  }

  /**
   * Double array value of the underlying {@link Tensor}, if {@link DataType} is {@code DOUBLE}.
   */
  public double[] doubleValue() {
    Preconditions.checkState(dataType() == DataType.DOUBLE);
    return (double []) data();
  }
}
