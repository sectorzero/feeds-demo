package org.sectorzero.core.articles;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRef implements Comparable<ArticleRef> {
  @JsonSerialize(using = DateTimeToStringSerializer.class)
  ZonedDateTime timestamp;
  String articleId;

  @Override
  public int compareTo(ArticleRef o) {
    if(o == null || o.getTimestamp() == null) {
      return 1;
    }
    return timestamp.compareTo(o.getTimestamp());
  }

  public static class DateTimeToStringSerializer extends JsonSerializer<ZonedDateTime> {
    @Override
    public void serialize(ZonedDateTime input,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
        throws IOException, JsonProcessingException {
      jsonGenerator.writeObject(input.toString());
    }
  }
}
