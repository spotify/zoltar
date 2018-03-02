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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemUtils {
  private FileSystemUtils() {}

  /**
   * If the path is not on a local filesystem, it will download the resource to a temporary path on
   * a local filesystem.
   */
  public static URI downloadIfNonLocal(URI path) throws IOException {
    FileSystem fs = FileSystems.get(path);
    if (fs instanceof LocalFileSystem) {
      return path;
    }
    Path temp = Files.createTempDirectory("model-serving-");
    for (Resource r : fs.list(path)) {
      String[] split = r.path().getPath().split("/");
      String filename = split[split.length - 1];
      Files.copy(r.open(), temp.resolve(filename));
    }
    return temp.toUri();
  }
}
