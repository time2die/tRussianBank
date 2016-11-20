package org.time2java.tRussianBank.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by time2die on 08.11.16.
 */
@Data
@Builder
@EqualsAndHashCode
public class Status {
    Integer bankCapital;
    Integer debt;
    Integer active;
    Integer bankAge;
    Integer memberNum;
    Integer city;
}
