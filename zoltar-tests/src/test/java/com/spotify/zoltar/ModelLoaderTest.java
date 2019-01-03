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

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class ModelLoaderTest {

  static class DummyModel implements Model<Object> {

    public DummyModel() {}

    @Override
    public Id id() {
      return Id.create("dummy");
    }

    @Override
    public Object instance() {
      return null;
    }

    @Override
    public void close() {}
  }

  @Test
  public void preload() throws InterruptedException, ExecutionException, TimeoutException {
    final ModelLoader<DummyModel> loader = ModelLoader
        .load(() -> {
          Thread.sleep(Duration.ofMillis(5).toMillis());
          return new DummyModel();
        }, ForkJoinPool.commonPool());

    final ModelLoader<DummyModel> preloaded = ModelLoader.preload(loader, Duration.ofSeconds(1));

    assertThat(preloaded.get().toCompletableFuture().isDone(), is(true));
  }

  @Test(expected = TimeoutException.class)
  public void preloadTimeout() throws InterruptedException, ExecutionException, TimeoutException {
    final ModelLoader<DummyModel> loader = ModelLoader
        .load(() -> {
          Thread.sleep(Duration.ofSeconds(10).toMillis());
          return new DummyModel();
        }, ForkJoinPool.commonPool());

    ModelLoader.preload(loader, Duration.ZERO);
  }

}
