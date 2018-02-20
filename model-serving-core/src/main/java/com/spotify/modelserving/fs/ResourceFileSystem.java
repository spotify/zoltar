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
