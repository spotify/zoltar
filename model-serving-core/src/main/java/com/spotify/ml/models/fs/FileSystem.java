package com.spotify.ml.models.fs;

import java.io.IOException;
import java.io.InputStream;

public interface FileSystem {

  InputStream open(String path) throws IOException;

}
