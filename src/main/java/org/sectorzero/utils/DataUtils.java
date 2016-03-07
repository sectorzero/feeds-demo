package org.sectorzero.utils;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;

import java.util.Map;
import java.util.Properties;

public class DataUtils {

  static public Properties p(Map<String, String> records) {
    Properties properties = new Properties();
    records.entrySet().stream()
        .forEach(e -> properties.setProperty(e.getKey(), e.getValue()));
    return properties;
  }

  static public Map<String, String> m(String... elements) {
    Validate.notNull(elements);
    Validate.isTrue(elements.length % 2 == 0);
    if(elements.length == 0) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, String> b = new ImmutableMap.Builder<>();
    for(int i = 0; i < elements.length; i += 2) {
      b.put(elements[i], elements[i+1]);
    }
    return b.build();
  }

}
