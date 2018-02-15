package com.spotify.modelserving;

import java.io.IOException;
import java.io.InputStream;

public interface FileSystem {

  InputStream open(String path) throws IOException;

}
