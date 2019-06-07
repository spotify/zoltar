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

import static com.spotify.zoltar.fs.FileSystemExtrasTestUtils.checkCopiedDirectory;
import static com.spotify.zoltar.fs.FileSystemExtrasTestUtils.jarUri;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class FileSystemExtrasTest {

  @Test
  public void localPath() throws IOException {
    final Path noSchema = FileSystemExtras.path(URI.create("/tmp"));
    assertNotNull(noSchema);

    final Path withSchema = FileSystemExtras.path(URI.create("file:///tmp"));
    assertNotNull(withSchema);
  }

  @Test
  public void jarPath() throws IOException {
    final Path jarPath = FileSystemExtras.path(jarUri());
    assertThat(jarPath, notNullValue());
  }

  @Test
  public void gcsPath() throws IOException {
    final Path gcsPath = FileSystemExtras.path(URI.create("gs://bucket/name"));
    assertThat(gcsPath.toString(), equalTo("/name"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidGcsBucketUri() throws IOException {
    FileSystemExtras.path(URI.create("gs://bucket_name"));
    fail("Should throw exception; bucket name is not rfc 2396 compliant");
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
  public void downloadIfNonLocalWithJar() throws IOException {
    final URI uri = FileSystemExtras.downloadIfNonLocal(jarUri());
    final File file = new File(uri);
    checkCopiedDirectory(file, "variables", "saved_model.pb", "trained_model.txt");
  }

  @Test
  public void copyDirectoryFromJar() throws IOException {
    final Path src = FileSystemExtras.path(jarUri());
    final Path dest = Files.createTempDirectory("zoltar-");
    final File file = FileSystemExtras.copyDir(src, dest, true).toFile();
    file.deleteOnExit();
    checkCopiedDirectory(file, "variables", "saved_model.pb", "trained_model.txt");
  }

  @Test
  public void copyDirectory() throws IOException, URISyntaxException {
    final URI resource = getClass().getResource("/trained_model").toURI();
    final Path src = new File(resource).toPath();
    final Path dest = Files.createTempDirectory("zoltar-");
    final File file = FileSystemExtras.copyDir(src, dest, true).toFile();
    file.deleteOnExit();
    checkCopiedDirectory(file, "variables", "saved_model.pb", "trained_model.txt");
  }

}
