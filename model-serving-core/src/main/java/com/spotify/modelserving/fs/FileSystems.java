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
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class FileSystems {

  private FileSystems() {}

  private static Map<String, FileSystem> mapping = ImmutableMap.of(
          "", LocalFileSystem.getInstance(),
          "file", LocalFileSystem.getInstance(),
          "gs", GcsFileSystem.getInstance(),
          "resource", ResourceFileSystem.getInstance());

  @VisibleForTesting
  static FileSystem get(String path) {
    String scheme = URI.create(path).getScheme();
    FileSystem fs = mapping.get(Objects.firstNonNull(scheme, ""));
    Preconditions.checkNotNull(fs, "Unsupported path: %s", path);
    return fs;
  }

  public static InputStream open(String path) throws IOException {
    return get(path).open(path);
  }

  public static String readString(String path) throws IOException {
    Scanner scanner = new Scanner(open(path), Charsets.UTF_8.name()).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }

  public static List<Resource> list(String path) throws IOException {
    return get(path).list(path);
  }
}
