package com.spotify.modelserving;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Test {
  public static void main(String[] args) throws IOException {
    System.out.println("hello");
    InputStream is = FileSystems.open("gs://scio-playground-neville/pom.xml");
    for (String s : CharStreams.readLines(new InputStreamReader(is))) {
      System.out.println(s);
    }
  }
}
