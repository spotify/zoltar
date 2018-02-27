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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public final class FileSystems {

  private FileSystems() {
  }

  private static final Map<String, Supplier<FileSystem>> mapping =
      ImmutableMap.of(
          "", LocalFileSystem::instance,
          "file", LocalFileSystem::instance,
          "gs", GcsFileSystem::instance,
          "resource", ResourceFileSystem::instance);

  @VisibleForTesting
  static FileSystem get(URI path) {
    final String scheme = path.getScheme();
    final FileSystem fs = mapping.get(Objects.firstNonNull(scheme, "")).get();

    Preconditions.checkNotNull(fs, "Unsupported path: %s", path);
    return fs;
  }

  public static InputStream open(URI path) throws IOException {
    return get(path).open(path);
  }

  public static List<Resource> list(URI path) throws IOException {
    return get(path).list(path);
  }
}
