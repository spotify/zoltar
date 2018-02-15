package com.spotify.modelserving.fs;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

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
    assertEquals("test", FileSystems.readString(relativePath.toString()));
    assertEquals("test", FileSystems.readString("file://" + absolutePath.toString()));
  }
}
