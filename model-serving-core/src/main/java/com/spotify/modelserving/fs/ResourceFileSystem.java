package com.spotify.modelserving.fs;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public final class ResourceFileSystem implements FileSystem {

  private static final ResourceFileSystem instance = new ResourceFileSystem();

  public static ResourceFileSystem getInstance() {
    return instance;
  }

  @Override
  public InputStream open(String path) throws IOException {
    final InputStream is = this.getClass().getResourceAsStream(parse(path));
    if (is == null) {
      throw new IOException("Resource not found");
    }

    return is;
  }

  @Override
  public List<Resource> list(String path) throws IOException {
    throw new UnsupportedOperationException("Cannot list resources");
  }

  private String parse(String path) {
    URI uri = URI.create(path);
    Preconditions.checkArgument("resource".equals(uri.getScheme()),
        "Not a resource path: %s", path);
    Preconditions.checkArgument(!uri.getPath().isEmpty(),
        "invalid resource: %s", path);

    return uri.getPath();
  }
}
