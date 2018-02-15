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
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

final class LocalFileSystem implements FileSystem {

  private static final LocalFileSystem instance = new LocalFileSystem();

  public static LocalFileSystem getInstance() {
    return instance;
  }

  @Override
  public InputStream open(String path) throws IOException {
    return new FileInputStream(parse(path));
  }

  @Override
  public List<Resource> list(String path) throws IOException {
    return Files.list(parse(path).toPath())
            .filter(p -> !Files.isDirectory(p))
            .map(p -> {
              try {
                return Resource.create(p.toString(), Files.getLastModifiedTime(p).toMillis());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(Collectors.toList());
  }

  private File parse(String path) {
    String p = path.startsWith("file:///") ? path.substring("file://".length()) : path;
    return new File(p);
  }
}
