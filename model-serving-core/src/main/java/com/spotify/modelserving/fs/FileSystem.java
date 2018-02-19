package com.spotify.modelserving.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileSystem {

  InputStream open(String path) throws IOException;

  List<Resource> list(String path) throws IOException;
}
