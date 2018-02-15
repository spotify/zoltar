package com.spotify.modelserving.fs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
}
