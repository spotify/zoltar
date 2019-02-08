/*-
 * -\-\-
 * zoltar-tests
 * --
 * Copyright (C) 2016 - 2019 Spotify AB
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class FileSystemExtrasTestUtils {

  public static void checkCopiedDirectory(final File file, String... items) {
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    final List<String> dirContents =
        Arrays.stream(file.listFiles()).map(File::getName).collect(Collectors.toList());

    assertThat(dirContents, containsInAnyOrder(items));
  }

  public static Path pathForJar() throws IOException {
    final String file = FileSystemExtrasTestUtils.class.getResource("/trained_model.jar").getFile();
    final URI uri = URI.create(String.format("jar:file:%s!/", file));
    return FileSystemExtras.path(uri);
  }

}
