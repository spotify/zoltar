/*-
 * -\-\-
 * zoltar-core
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class FileSystemExtrasTest {

  @Test
  public void getPath() {
    final Path noSchema = FileSystemExtras.path(URI.create("/tmp"));
    assertNotNull(noSchema);

    final Path withSchema = FileSystemExtras.path(URI.create("file:///tmp"));
    assertNotNull(withSchema);
  }

  @Test
  public void getLatestDateTest() throws IOException, URISyntaxException {
    final String abspath = new File(getClass().getResource("/testdir").toURI()).getAbsolutePath();
    assertEquals(FileSystemExtras.getLatestDate(abspath).get(), "2018-03-01");
  }

  @Test(expected = IOException.class)
  public void getLatestDateTestInputDoesNotExist() throws IOException {
    FileSystemExtras.getLatestDate("toastdir");
  }

  @Test(expected = IOException.class)
  public void getLatestDateTestInputNotADirectory() throws IOException {
    FileSystemExtras.getLatestDate("test.txt");
  }

  @Test(expected = IOException.class)
  public void getLatestDateTestInputEmptyDir() throws IOException {
    FileSystemExtras.getLatestDate("/emptydir");
  }

  @Test(expected = IOException.class)
  public void getLatestDateTestInputBadSubdirs() throws IOException {
    FileSystemExtras.getLatestDate("/badsubdir");
  }

  @Test
  public void noCopyIfDefaultFileSystem() throws IOException {
    final URI uri = FileSystemExtras.downloadIfNonLocal(URI.create("/tmp"));
    assertFalse(new File(uri).getName().startsWith("zoltar"));
  }

  @Test
  public void copyDirectory() throws IOException {
    final Path temp = Files.createTempDirectory("original-zoltar");
    new File(temp.toFile(), "foobar").createNewFile();

    final Path dest = Files.createTempDirectory("zoltar-");
    final Path path = FileSystemExtras.copyDir(temp, dest, true);

    assertTrue(path.toFile().getName().startsWith("zoltar"));
    assertTrue(path.toFile().exists());
    assertTrue(path.toFile().isDirectory());
    assertThat(path.toFile().listFiles().length, is(1));

    dest.toFile().deleteOnExit();
    temp.toFile().deleteOnExit();
    path.toFile().deleteOnExit();
  }
}
