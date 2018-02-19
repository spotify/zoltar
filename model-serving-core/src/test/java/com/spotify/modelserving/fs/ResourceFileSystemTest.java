package com.spotify.modelserving.fs;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ResourceFileSystemTest {

  @Test
  public void testReadStream() throws IOException {
    assertEquals("test", FileSystems.readString("resource:///test.txt"));
  }

  @Test(expected = IOException.class)
  public void testResourceNotFound() throws IOException {
    FileSystems.readString("resource:///notfound.txt");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidResourcePath() throws IOException {
    FileSystems.readString("resource://test.txt");
  }

}
