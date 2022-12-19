package org.neo4j.ogm.domain.dto;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.LocalDateStringConverter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Gerrit Meier
 */
public class SomeDto {

    public String valueA;
    public Integer valueB;
    @Convert(LocalDateStringConverter.class)
    public LocalDate valueC;
    public List<String> valueD;
}
