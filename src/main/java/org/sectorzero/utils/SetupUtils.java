package org.sectorzero.utils;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.IOException;

public class SetupUtils {

  static public File createDirIfNotExists(String path) throws IOException {
    Validate.notEmpty(path);
    File d = new File(path);
    if (d.exists()) {
      Validate.isTrue(d.isDirectory());
      return d;
    }
    return java.nio.file.Files.createDirectories(d.toPath()).toFile();
  }
}
