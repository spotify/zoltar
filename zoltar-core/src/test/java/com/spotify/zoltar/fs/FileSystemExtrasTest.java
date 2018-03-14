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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class FileSystemExtrasTest {

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
}
