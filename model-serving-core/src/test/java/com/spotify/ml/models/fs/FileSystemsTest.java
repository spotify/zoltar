package com.spotify.ml.models.fs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSystemsTest {
  @Test
  public void testGet() {
    assertEquals(FileSystems.get("input.txt").getClass(), LocalFileSystem.class);
    assertEquals(FileSystems.get("file:///input.txt").getClass(), LocalFileSystem.class);
    assertEquals(FileSystems.get("gs://bucket/input.txt").getClass(), GcsFileSystem.class);
    assertEquals(FileSystems.get("resource:///input.txt").getClass(), ResourceFileSystem.class);
  }
}
