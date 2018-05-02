/*-
 * -\-\-
 * zoltar-tensorflow
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

package com.spotify.zoltar.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FileSystem utils and extras.
 */
public final class FileSystemExtras {

  // Suppresses default constructor, ensuring non-instantiability.
  private FileSystemExtras() {
  }


  /**
   * Finds the latest date directory in the directory src. It assumes that partitions are formatted
   * using ISO_LOCAL_DATE (e.g. YYYY-MM-DD).
   */
  public static Optional<String> getLatestDate(final String src) throws IOException {
    return Files.list(Paths.get(src))
        .filter(Files::isDirectory)
        .map(p -> LocalDate.parse(p.getFileName().toString()))
        .reduce((x, y) -> x.compareTo(y) > 0 ? x : y)
        .map(LocalDate::toString);
  }

  /**
   * Creates a {@link Path} given a {@link URI} in a user-friendly and safe way.
   */
  public static Path path(final URI uri) {
    if (uri.getScheme() == null) {
      return Paths.get(uri.toString());
    }

    return Paths.get(uri);
  }

  /**
   * If the path is not on a local filesystem, it will download the resource to a temporary path on
   * the local filesystem.
   *
   * <p>NOTE: Zoltar internal use only!</p>
   */
  public static URI downloadIfNonLocal(final URI path) throws IOException {
    final String fixedPath =
        path.toString().endsWith("/") ? path.toString() : path.toString() + "/";
    final Path src = path(URI.create(fixedPath));
    if (src.getFileSystem().equals(FileSystems.getDefault())) {
      return src.toUri();
    }

    final Path temp = Files.createTempDirectory("zoltar-");
    return copyDir(src, temp, true).toUri();
  }

  static Path copyDir(final Path src, final Path dest, final boolean overwrite)
      throws IOException {
    final List<URI> uris = Files.walk(src)
        .filter(path -> !path.equals(src))
        .map(Path::toUri)
        .collect(Collectors.toList());

    for (final URI uri : uris) {
      final String relative =
          uri.toString().substring(src.toUri().toString().length(), uri.toString().length());
      final Path fullDst = Paths.get(dest.toUri().resolve(relative));
      final CopyOption[] flags = overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING}
          : new CopyOption[]{};
      Files.copy(Paths.get(uri), fullDst, flags);
    }

    return dest;
  }
}
