package com.spotify.modelserving.fs;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public final class ResourceFileSystem implements FileSystem {

  private static final ResourceFileSystem instance = new ResourceFileSystem();

  public static ResourceFileSystem getInstance() {
    return instance;
  }

  @Override
  public InputStream open(String path) throws IOException {
    return this.getClass().getResourceAsStream(parse(path));
  }

  private String parse(String path) {
    URI uri = URI.create(path);
    Preconditions.checkArgument("resource".equals(uri.getScheme()),
            "Not a resource path: %s", path);
    return uri.getPath();
  }
}
