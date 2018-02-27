package com.spotify.modelserving.fs;

import static org.junit.Assert.assertEquals;

import com.spotify.modelserving.fs.Resource.ReadFns;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileSystemTest {

  private static Path relativePath;
  private static Path absolutePath;

  @BeforeClass
  public static void setup() throws IOException {
    relativePath = Paths.get(UUID.randomUUID().toString() + ".txt");
    BufferedWriter writer1 = Files.newBufferedWriter(relativePath);
    writer1.write("test");
    writer1.close();

    absolutePath = Files.createTempFile("local-file-system-test-", ".txt");
    BufferedWriter writer2 = Files.newBufferedWriter(absolutePath);
    writer2.write("test");
    writer2.close();
  }

  @AfterClass
  public static void tearDown() throws IOException {
    Files.delete(relativePath);
    Files.delete(absolutePath);
  }

  @Test
  public void testReadString() throws IOException {
    assertEquals("test", Resource.from(relativePath.toUri()).read(ReadFns.asString()));
    assertEquals("test",
        Resource.from(URI.create("file://" + absolutePath.toString())).read(ReadFns.asString()));
  }
}
