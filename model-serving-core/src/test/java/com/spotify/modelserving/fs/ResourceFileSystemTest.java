package com.spotify.modelserving.fs;

import static org.junit.Assert.assertEquals;

import com.spotify.modelserving.fs.Resource.ReadFns;
import java.io.IOException;
import java.net.URI;
import org.junit.Test;

public class ResourceFileSystemTest {

  @Test
  public void testReadStream() throws IOException {
    assertEquals("test", Resource.from(URI.create("resource:///test.txt")).read(ReadFns.asString()));
  }

  @Test(expected = IOException.class)
  public void testResourceNotFound() throws IOException {
    Resource.from(URI.create("resource:///notfound.txt")).read(ReadFns.asString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidResourcePath() throws IOException {
    Resource.from(URI.create("resource://test.txt")).read(ReadFns.asString());
  }

}
