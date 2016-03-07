package org.sectorzero.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feed {

  @JsonProperty
  String name;

  @JsonProperty
  List<String> articleIds;

}
