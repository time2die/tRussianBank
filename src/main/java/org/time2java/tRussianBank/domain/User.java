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
public class User {
    String name ;
    String vkID ;
    String city ;
    Integer paymentNum ;
    Integer paymentSum ;
    Integer debtCount ;
    Integer currentDeb ;
    Integer earlyReturn ;
    Integer delayReturn ;
    Boolean hasLastMounthsPays ;
}
