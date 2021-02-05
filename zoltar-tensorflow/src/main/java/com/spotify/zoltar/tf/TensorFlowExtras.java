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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.tensorflow.Session;
import org.tensorflow.Tensor;

import com.google.common.collect.Maps;

/** TensorFlow utilities and extras. */
public class TensorFlowExtras {

  private TensorFlowExtras() {}

  /**
   * Fetch a list of operations from a {@link Session.Runner} and return the respective {@link
   * Tensor}.
   *
   * @param runner {@link Session.Runner} to fetch operations and extract outputs from.
   * @param fetchOps operations to fetch.
   * @return a {@link Map} of operations and output {@link Tensor}s. Map keys are in the same order
   *     as {@code fetchOps}.
   */
  public static <A> A runAndExtract(
      final Session.Runner runner,
      final List<String> fetchOps,
      Function<Map<String, Tensor<?>>, A> fn) {
    for (final String op : fetchOps) {
      runner.fetch(op);
    }
    final Map<String, Tensor<?>> tensorMap = Maps.newLinkedHashMapWithExpectedSize(fetchOps.size());
    final List<Tensor<?>> tensors = runner.run();

    for (int i = 0; i < fetchOps.size(); i++) {
      tensorMap.put(fetchOps.get(i), tensors.get(i));
    }

    A result = fn.apply(tensorMap);

    tensorMap.values().forEach(Tensor::close);

    return result;
  }
}
