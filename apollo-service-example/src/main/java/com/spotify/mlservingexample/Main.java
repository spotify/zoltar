package com.spotify.mlservingexample;

import com.spotify.apollo.Environment;
import com.spotify.apollo.standalone.LoadingException;
import com.spotify.apollo.standalone.StandaloneService;

/** Application entry point. */
public final class Main {

  static final String SERVICE_NAME = "ml-serving-example";

  private Main() {}

  /**
   * Runs the application.
   *
   * @param args command-line arguments
   */
  public static void main(final String... args) throws LoadingException {
    StandaloneService.boot(Main::configure, SERVICE_NAME, args);
  }

  static void configure(final Environment environment) {}
}
