package com.spotify.modelserving.fs;

import com.google.auto.value.AutoValue;
import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Scanner;

@AutoValue
public abstract class Resource {

  public abstract URI path();

  public abstract Instant timestamp();

  public abstract FileSystem fileSystem();

  public static Resource create(URI path, Instant timestamp, FileSystem fs) {
    return new AutoValue_Resource(path, timestamp, fs);
  }

  public static Resource from(String path) {
    URI uri = URI.create(path);
    return create(uri, Instant.EPOCH, FileSystems.get(uri));
  }

  public static Resource from(URI path) {
    return create(path, Instant.EPOCH, FileSystems.get(path));
  }

  @FunctionalInterface
  public interface ReadFn<T> {

    T read(Resource resource) throws IOException;

  }

  public interface ReadFns {

    static ReadFn<String> asString() throws IOException {
      return resource -> {
        Scanner scanner = new Scanner(resource.open(), Charsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
      };
    }

    static ReadFn<InputStream> asStream() throws IOException {
      return Resource::open;
    }

  }

  public <T> T read(ReadFn<T> readFn) throws IOException {
    return readFn.read(this);
  }

  public InputStream open() throws IOException {
    return fileSystem().open(path());
  }

}
