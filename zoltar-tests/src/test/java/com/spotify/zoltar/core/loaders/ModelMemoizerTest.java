/*-
 * -\-\-
 * zoltar-core
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

package com.spotify.zoltar.core.loaders;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.spotify.zoltar.core.Model;
import com.spotify.zoltar.core.ModelLoader;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ModelMemoizerTest {

  static class DummyModel implements Model<Object> {
    private final AtomicInteger inc;

    public DummyModel() {
      inc = new AtomicInteger();
    }

    @Override
    public Id id() {
      return Id.create("dummy");
    }

    @Override
    public Object instance() {
      inc.getAndIncrement();
      return null;
    }

    @Override
    public void close() throws Exception {

    }

    public int getIncrementValue() {
      return inc.get();
    }
  }

  @Test
  public void memoize() throws InterruptedException, ExecutionException, TimeoutException {
    final ModelLoader<DummyModel> loader =
        ModelLoader.lift(DummyModel::new).with(ModelMemoizer::memoize);

    final Duration duration = Duration.ofMillis(1000);
    loader.get(duration).instance();
    loader.get(duration).instance();

    assertThat(loader.get(duration).getIncrementValue(), is(2));
  }
}
