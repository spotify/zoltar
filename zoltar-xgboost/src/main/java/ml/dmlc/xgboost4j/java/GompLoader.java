/*-
 * -\-\-
 * zoltar-xgboost
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

package ml.dmlc.xgboost4j.java;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

public class GompLoader {

  private static boolean isLinux() {
    return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("linux");
  }

  private static void loadGomp() throws IOException {
    if (isLinux()) {
      load("/lib/libgomp.so.1");
    }
  }

  private static void load(String name) throws IOException {
    try {
      Vector<String> libraries = getLoadedLibraries(ClassLoader.getSystemClassLoader());
      System.out.println("Loaded libs" + libraries);
      String[] libs = libraries.toArray(new String[libraries.size()]);

      String libFile = name.substring(name.lastIndexOf('/') + 1, name.length());
      String libName = libFile.substring(0, libFile.indexOf('.'));
      if (!Arrays.stream(libs).anyMatch(n -> n.contains(libName))) {
        System.load(NativeLibLoader.createTempFileFromResource(name));
      }
    } catch (IOException e) {
      throw new IOException(e);
    }

  }

  private static Vector<String> getLoadedLibraries(ClassLoader loader) throws IOException {
    try {
      Field libField = ClassLoader.class.getDeclaredField("loadedLibraryNames");
      libField.setAccessible(true);
      Vector<String> libraries = (java.util.Vector<String>) libField.get(loader);

      return libraries;
    } catch (NoSuchFieldException noSuchFieldError) {
      throw new IOException(noSuchFieldError);
    } catch (IllegalAccessException illAccessError) {
      throw new IOException(illAccessError);
    }
  }

  public static void start() throws IOException {
    if (isLinux()) {
      loadGomp();
      load("/lib/libxgboost4j.so");
    } else {
      load("/lib/libxgboost4j.dylib");
    }
  }
}
