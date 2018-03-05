/*-
 * -\-\-
 * model-serving-core
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

/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.modelserving.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

final class LocalFileSystem implements FileSystem {

  private static final LocalFileSystem instance = new LocalFileSystem();

  public static LocalFileSystem instance() {
    return instance;
  }

  @Override
  public InputStream open(URI path) throws IOException {
    return new FileInputStream(new File(path));
  }

  @Override
  public List<Resource> list(URI path) throws IOException {
    return Files.list(new File(path).toPath())
        .filter(p -> !Files.isDirectory(p))
        .map(p -> {
          try {
            final Instant instant = Files.getLastModifiedTime(p).toInstant();
            return Resource.create(URI.create(p.toString()), instant, this);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }

}
