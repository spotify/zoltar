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
import java.util.List;
import java.util.stream.Collectors;

public final class FileSystemExtras {

  private FileSystemExtras() {
  }

  public static Path path(final URI uri) {
    if (uri.getScheme() == null) {
      return Paths.get(uri.toString());
    }

    return Paths.get(uri);
  }

  /**
   * If the path is not on a local filesystem, it will download the resource to a temporary path on
   * a local filesystem.
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

  private static Path copyDir(final Path src, final Path dest, final boolean overwrite)
      throws IOException {
    final List<Path> paths = Files.walk(src)
        .filter(path -> !path.equals(src))
        .collect(Collectors.toList());

    for (final Path path : paths) {
      final String relative = path.toString().substring(src.toString().length() - 1);
      final Path fullDst = Paths.get(dest.toString(), relative);
      final CopyOption[] flags = overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING}
          : new CopyOption[]{};
      Files.copy(path, fullDst, flags);
    }

    return dest;
  }
}
