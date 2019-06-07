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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.junit.Test;

public class FileSystemExtrasTestIT {

  @Test
  public void downloadGcsBucket() throws IOException {
    final URI gcsUri = URI.create(
        "gs://data-integration-test-us/zoltar/iris/trained/regadas/2018-04-16--14-47-55/export/1523904529/");
    final File local = new File(FileSystemExtras.downloadIfNonLocal(gcsUri));
    local.deleteOnExit();

    checkCopiedDirectory(local, "variables", "saved_model.pb");
  }

}
