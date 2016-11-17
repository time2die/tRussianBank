package org.time2java.tRussianBank.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by time2die on 06.11.16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class gaAnswer {
    @JsonProperty("range")
    private String range;
    @JsonProperty("majorDimension")
    private String majorDimension;
    @JsonProperty("values")
    private List<List<String>> values;
}
