package com.Tsimur.Dubcast.mapper;

import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface DateTimeMapper {

    default Instant asInstant(OffsetDateTime odt) {
        return odt != null ? odt.toInstant() : null;
    }

    default OffsetDateTime asOffsetDateTime(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}