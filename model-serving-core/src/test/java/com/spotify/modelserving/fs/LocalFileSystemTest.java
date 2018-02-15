package com.spotify.modelserving.fs;

import org.junit.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
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
  public void testOpen() throws IOException {
    assertEquals("test", readAll(FileSystems.open(relativePath.toString())));
    assertEquals("test", readAll(FileSystems.open("file://" + absolutePath.toString())));
  }

  private String readAll(InputStream is) {
    Scanner scanner = new Scanner(is).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }
}
