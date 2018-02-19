package com.spotify.modelserving.fs;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Resource {

  public abstract String path();

  public abstract long timestamp();

  public static Resource create(String path, long timestamp) {
    return new AutoValue_Resource(path, timestamp);
  }

}
