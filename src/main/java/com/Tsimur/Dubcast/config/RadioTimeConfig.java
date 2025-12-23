package com.Tsimur.Dubcast.config;

import java.time.ZoneId;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RadioTimeConfig {

  private final ZoneId radioZoneId;

  public RadioTimeConfig(@Value("${radio.timezone:Europe/Vilnius}") String tz) {
    this.radioZoneId = ZoneId.of(tz);
  }
}
