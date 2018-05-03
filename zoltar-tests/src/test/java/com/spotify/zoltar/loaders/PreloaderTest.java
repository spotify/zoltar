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

package com.spotify.zoltar.loaders;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.spotify.zoltar.Model;
import com.spotify.zoltar.ModelLoader;
import java.time.Duration;
import org.junit.Test;

public class PreloaderTest {

  static class DummyModel implements Model<Object> {

    public DummyModel() {
    }

    @Override
    public Id id() {
      return Id.create("dummy");
    }

    @Override
    public Object instance() {
      return null;
    }

    @Override
    public void close() throws Exception {

    }
  }

  @Test
  public void preload() {
    final ModelLoader<DummyModel> loader = ModelLoader
        .lift(DummyModel::new)
        .with(Preloader.preload());

    assertThat(loader.get().toCompletableFuture().isDone(), is(true));
  }

  @Test
  public void preloadTimeout() {
    final ModelLoader<DummyModel> loader = ModelLoader
        .lift(() -> {
          Thread.sleep(Duration.ofSeconds(10).toMillis());
          return new DummyModel();
        })
        .with(Preloader.preload(Duration.ZERO));

    assertThat(loader.get().toCompletableFuture().isCompletedExceptionally(), is(true));
  }

  @Test
  public void preloadAsync() {
    final ModelLoader<DummyModel> loader = ModelLoader
        .lift(() -> {
          Thread.sleep(Duration.ofSeconds(10).toMillis());
          return new DummyModel();
        })
        .with(Preloader.preloadAsync());

    assertThat(loader.get().toCompletableFuture().isDone(), is(false));
  }
}
