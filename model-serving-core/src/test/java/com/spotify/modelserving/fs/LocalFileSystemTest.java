/*-
 * -\-\-
 * model-serving-core
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

package com.spotify.modelserving.fs;

import static org.junit.Assert.assertEquals;

import com.spotify.modelserving.fs.Resource.ReadFns;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileSystemTest {

  private static Path relativePath;
  private static Path absolutePath;

  @BeforeClass
  public static void setup() throws IOException {
    relativePath = Paths.get(UUID.randomUUID().toString() + ".txt");
    BufferedWriter writer1 = Files.newBufferedWriter(relativePath);
    writer1.write("test");
    writer1.close();

    absolutePath = Files.createTempFile("local-file-system-test-", ".txt");
    BufferedWriter writer2 = Files.newBufferedWriter(absolutePath);
    writer2.write("test");
    writer2.close();
  }

  @AfterClass
  public static void tearDown() throws IOException {
    Files.delete(relativePath);
    Files.delete(absolutePath);
  }

  @Test
  public void testReadString() throws IOException {
    assertEquals("test", Resource.from(relativePath.toUri()).read(ReadFns.asString()));
    assertEquals("test",
        Resource.from(URI.create("file://" + absolutePath.toString())).read(ReadFns.asString()));
  }
}
