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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat64;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TensorFlowExtrasTest {

  private static final String mul2 = "mul2";
  private static final String mul3 = "mul3";

  private static Graph createDummyGraph() {
    final Tensor t2 = TFloat64.scalarOf(2.0);
    final Tensor t3 = TFloat64.scalarOf(3.0);

    final Graph graph = new Graph();
    final Output<TFloat64> input =
        graph
            .opBuilder("Placeholder", "input")
            .setAttr("dtype", DataType.DT_DOUBLE)
            .build()
            .output(0);

    final Output<TFloat64> two =
        graph
            .opBuilder("Const", "two")
            .setAttr("dtype", t2.dataType())
            .setAttr("value", t2)
            .build()
            .output(0);

    final Output<TFloat64> three =
        graph
            .opBuilder("Const", "three")
            .setAttr("dtype", t3.dataType())
            .setAttr("value", t3)
            .build()
            .output(0);

    graph.opBuilder("Mul", mul2).addInput(input).addInput(two).build();

    graph.opBuilder("Mul", mul3).addInput(input).addInput(three).build();

    return graph;
  }

  @Test
  public void testExtract1() {
    final Graph graph = createDummyGraph();
    final Session session = new Session(graph);
    final Session.Runner runner = session.runner();
    runner.feed("input", TFloat64.scalarOf(10.0));

    TensorFlowExtras.runAndExtract(
        runner,
        Collections.singletonList(mul2),
        result -> {
          assertEquals(Sets.newHashSet(mul2), result.keySet());
          assertScalar(result.get(mul2), 20.0);

          return null;
        });

    session.close();
    graph.close();
  }

  @Test
  public void testExtract2a() {
    final Graph graph = createDummyGraph();
    final Session session = new Session(graph);
    final Session.Runner runner = session.runner();
    runner.feed("input", TFloat64.scalarOf(10.0));

    TensorFlowExtras.runAndExtract(
        runner,
        ImmutableList.of(mul2, mul3),
        result -> {
          assertEquals(Lists.newArrayList(mul2, mul3), new ArrayList<>(result.keySet()));
          assertScalar(result.get(mul2), 20.0);
          assertScalar(result.get(mul3), 30.0);

          return null;
        });

    session.close();
    graph.close();
  }

  @Test
  public void testExtract2b() {
    final Graph graph = createDummyGraph();
    final Session session = new Session(graph);
    final Session.Runner runner = session.runner();
    runner.feed("input", TFloat64.scalarOf(10.0));

    TensorFlowExtras.runAndExtract(
        runner,
        ImmutableList.of(mul3, mul2),
        result -> {
          assertEquals(Lists.newArrayList(mul3, mul2), new ArrayList<>(result.keySet()));
          assertScalar(result.get(mul2), 20.0);
          assertScalar(result.get(mul3), 30.0);

          return null;
        });

    session.close();
    graph.close();
  }

  private void assertScalar(final Tensor jt, final Double value) {
    assertEquals(0, jt.shape().numDimensions());
    assertEquals(0, jt.shape().asArray().length);
    assertEquals(value, ((TFloat64) jt).getDouble(), 0.0);
  }
}
