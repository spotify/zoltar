package com.spotify.modelserving.fs;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

class GcsFileSystem implements FileSystem {

  private static final GcsFileSystem instance = new GcsFileSystem();

  private final Storage storage;

  private GcsFileSystem() {
    try {
      NetHttpTransport transport = new NetHttpTransport();
      JacksonFactory jacksonFactory = new JacksonFactory();
      GoogleCredential credential = GoogleCredential.getApplicationDefault();
      storage = new Storage(transport, jacksonFactory, credential);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static GcsFileSystem getInstance() {
    return instance;
  }

  @Override
  public InputStream open(String path) throws IOException {
    ObjectId id = parse(path);
    return storage.objects().get(id.bucket, id.path).executeMedia().getContent();
  }

  private ObjectId parse(String path) {
    URI uri = URI.create(path);
    Preconditions.checkArgument("gs".equals(uri.getScheme()),
            "Not a GCS path: %s", path);
    return new ObjectId(uri.getHost(), uri.getPath().substring(1));
  }

  private class ObjectId {
    final String bucket;
    final String path;

    private ObjectId(String bucket, String path) {
      this.bucket = bucket;
      this.path = path;
    }
  }
}
