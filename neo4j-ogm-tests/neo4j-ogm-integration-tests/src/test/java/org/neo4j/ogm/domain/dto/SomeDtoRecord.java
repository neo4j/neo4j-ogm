package org.neo4j.ogm.domain.dto;

import java.time.LocalDate;

/**
 * @author Gerrit Meier
 */
public record SomeDtoRecord(String valueA, Integer valueB, LocalDate valueC) {
}
