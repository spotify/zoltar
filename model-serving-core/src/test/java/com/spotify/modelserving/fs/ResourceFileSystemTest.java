package com.spotify.modelserving.fs;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ResourceFileSystemTest {
  @Test
  public void testReadStream() throws IOException {
    assertEquals("test", FileSystems.readString("resource:///test.txt"));
  }
}
