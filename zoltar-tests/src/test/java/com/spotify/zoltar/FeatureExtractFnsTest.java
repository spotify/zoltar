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
package com.spotify.zoltar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import com.spotify.zoltar.FeatureExtractFns.ExtractFn;

public class FeatureExtractFnsTest {

  @SuppressWarnings("unchecked")
  @Test
  public void batchExtractFn() throws Exception {
    final ExtractFn<Integer, Double> fn = ExtractFn.extract(Integer::doubleValue);

    final List<Vector<Integer, Double>> extracted =
        fn.apply(ImmutableList.of(1, 2)).toCompletableFuture().get();
    final List<Vector<Integer, Double>> expected =
        ImmutableList.of(Vector.create(1, 1d), Vector.create(2, 2d));

    assertThat(extracted, is(expected));
  }
}
