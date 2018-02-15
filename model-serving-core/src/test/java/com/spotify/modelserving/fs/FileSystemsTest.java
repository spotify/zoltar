package com.spotify.modelserving.fs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSystemsTest {
  @Test
  public void testGet() {
    assertEquals(LocalFileSystem.class, FileSystems.get("input.txt").getClass());
    assertEquals(LocalFileSystem.class, FileSystems.get("file:///input.txt").getClass());
    assertEquals(GcsFileSystem.class, FileSystems.get("gs://bucket/input.txt").getClass());
    assertEquals(ResourceFileSystem.class, FileSystems.get("resource:///input.txt").getClass());
  }
}
