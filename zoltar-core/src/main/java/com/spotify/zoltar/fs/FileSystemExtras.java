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
package com.spotify.zoltar.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.cloud.storage.contrib.nio.CloudStorageFileSystem;

/** FileSystem utils and extras. */
public final class FileSystemExtras {

  // Suppresses default constructor, ensuring non-instantiability.
  private FileSystemExtras() {}

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

  /** Creates a {@link Path} given a {@link URI} in a user-friendly and safe way. */
  public static Path path(final URI uri) throws IOException {
    final String scheme = uri.getScheme();

    // uri with no scheme might be a local path
    if (scheme == null) {
      return Paths.get(uri.toString());
    }

    try {
      return Paths.get(uri);
    } catch (FileSystemNotFoundException e) {
      // If the URI is a jar/zip file, register it as a file system so Paths.get is able to access
      // the files in it. See Oracle doc for more details:
      // https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
      FileSystems.newFileSystem(uri, Collections.emptyMap());
      return Paths.get(uri);
    } catch (IllegalArgumentException e) {
      if (uri.getHost() == null && scheme.equalsIgnoreCase(CloudStorageFileSystem.URI_SCHEME)) {
        // https://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_hostnames
        throw new IllegalArgumentException("gcs bucket is not rfc 2396 compliant");
      }
      throw e;
    }
  }

  /**
   * If the path is not on a local filesystem, it will download the resource to a temporary path on
   * the local filesystem.
   *
   * <p>NOTE: Zoltar internal use only!
   */
  public static URI downloadIfNonLocal(final URI path) throws IOException {
    final Path src = path(path);
    if (src.getFileSystem().equals(FileSystems.getDefault())) {
      return src.toUri();
    }

    final Path temp = Files.createTempDirectory("zoltar-");
    return copyDir(src, temp, true).toUri();
  }

  static Path copyDir(final Path src, final Path dest, final boolean overwrite) throws IOException {
    final List<Path> paths =
        Files.walk(src).filter(path -> !path.equals(src)).collect(Collectors.toList());

    for (final Path path : paths) {
      // The relativize method requires that src and path are either both absolute or both relative.
      // For GCS, src will be an absolute CloudStoragePath object, and path will be a relative
      // UnixPath object. To avoid any mismatch, always make them both absolute.
      final Path relative = src.toAbsolutePath().relativize(path.toAbsolutePath());
      // The resolve method can be passed a String or a Path - we must pass String. If copying
      // from a jar file, the relative path will be ZipPath, while our destination directory will
      // be UnixPath. This difference will cause resolve to throw ProviderMismatchException, so
      // we must first convert relative to String.
      final Path fullDst = dest.resolve(relative.toString());
      final CopyOption[] flags =
          overwrite ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING} : new CopyOption[] {};
      Files.copy(path, fullDst, flags);
    }

    return dest;
  }
}
