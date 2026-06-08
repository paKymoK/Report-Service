package com.takypok.report.model.enums;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostgresTimezone {

  // UTC-8 to UTC-5 (Americas - Pacific to Eastern)
  AMERICA_LOS_ANGELES("America/Los_Angeles", -8, 0), // Has DST (PST/PDT)
  AMERICA_VANCOUVER("America/Vancouver", -8, 0), // Has DST
  AMERICA_SEATTLE("America/Seattle", -8, 0), // Has DST
  AMERICA_DENVER("America/Denver", -7, 0), // Has DST (MST/MDT)
  AMERICA_PHOENIX("America/Phoenix", -7, 0), // No DST
  AMERICA_CHICAGO("America/Chicago", -6, 0), // Has DST (CST/CDT)
  AMERICA_MEXICO_CITY("America/Mexico_City", -6, 0), // Has DST
  AMERICA_NEW_YORK("America/New_York", -5, 0), // Has DST (EST/EDT)
  AMERICA_TORONTO("America/Toronto", -5, 0), // Has DST
  AMERICA_MONTREAL("America/Montreal", -5, 0), // Has DST
  AMERICA_DETROIT("America/Detroit", -5, 0), // Has DST
  AMERICA_BOGOTA("America/Bogota", -5, 0),
  AMERICA_LIMA("America/Lima", -5, 0),

  // UTC-4 to UTC-3 (South America)
  AMERICA_CARACAS("America/Caracas", -4, 0),
  AMERICA_SAO_PAULO("America/Sao_Paulo", -3, 0), // Has DST
  AMERICA_BUENOS_AIRES("America/Buenos_Aires", -3, 0),
  AMERICA_SANTIAGO("America/Santiago", -3, 0), // Has DST

  // UTC+0 (Western Europe & UTC)
  UTC("UTC", 0, 0),
  EUROPE_LONDON("Europe/London", 0, 0), // Has DST (GMT/BST)
  EUROPE_DUBLIN("Europe/Dublin", 0, 0), // Has DST
  EUROPE_LISBON("Europe/Lisbon", 0, 0), // Has DST

  // UTC+1 (Central Europe & Africa)
  AFRICA_LAGOS("Africa/Lagos", 1, 0),
  EUROPE_PARIS("Europe/Paris", 1, 0), // Has DST
  EUROPE_BERLIN("Europe/Berlin", 1, 0), // Has DST
  EUROPE_ROME("Europe/Rome", 1, 0), // Has DST
  EUROPE_MADRID("Europe/Madrid", 1, 0), // Has DST
  EUROPE_AMSTERDAM("Europe/Amsterdam", 1, 0), // Has DST
  EUROPE_BRUSSELS("Europe/Brussels", 1, 0), // Has DST
  EUROPE_VIENNA("Europe/Vienna", 1, 0), // Has DST
  EUROPE_ZURICH("Europe/Zurich", 1, 0), // Has DST
  EUROPE_PRAGUE("Europe/Prague", 1, 0), // Has DST
  EUROPE_WARSAW("Europe/Warsaw", 1, 0), // Has DST
  EUROPE_STOCKHOLM("Europe/Stockholm", 1, 0), // Has DST
  EUROPE_OSLO("Europe/Oslo", 1, 0), // Has DST
  EUROPE_COPENHAGEN("Europe/Copenhagen", 1, 0), // Has DST

  // UTC+2 (Eastern Europe & Africa)
  AFRICA_JOHANNESBURG("Africa/Johannesburg", 2, 0),
  AFRICA_CAIRO("Africa/Cairo", 2, 0),
  EUROPE_ATHENS("Europe/Athens", 2, 0), // Has DST
  EUROPE_HELSINKI("Europe/Helsinki", 2, 0), // Has DST
  EUROPE_BUCHAREST("Europe/Bucharest", 2, 0), // Has DST
  EUROPE_KIEV("Europe/Kiev", 2, 0), // Has DST
  ASIA_JERUSALEM("Asia/Jerusalem", 2, 0), // Has DST
  ASIA_BEIRUT("Asia/Beirut", 2, 0), // Has DST

  // UTC+3 (Eastern Europe, Middle East, East Africa)
  AFRICA_NAIROBI("Africa/Nairobi", 3, 0),
  EUROPE_MOSCOW("Europe/Moscow", 3, 0),
  ASIA_ISTANBUL("Europe/Istanbul", 3, 0),
  ASIA_RIYADH("Asia/Riyadh", 3, 0),
  ASIA_KUWAIT("Asia/Kuwait", 3, 0),
  ASIA_QATAR("Asia/Qatar", 3, 0),
  ASIA_BAHRAIN("Asia/Bahrain", 3, 0),

  // UTC+4 (Middle East)
  ASIA_DUBAI("Asia/Dubai", 4, 0),

  // UTC+5 (Central Asia, South Asia)
  ASIA_KARACHI("Asia/Karachi", 5, 0),
  ASIA_TASHKENT("Asia/Tashkent", 5, 0),

  // UTC+5:30 (India, Sri Lanka)
  ASIA_KOLKATA("Asia/Kolkata", 5, 30),
  ASIA_COLOMBO("Asia/Colombo", 5, 30),

  // UTC+6 (Central Asia, Bangladesh)
  ASIA_DHAKA("Asia/Dhaka", 6, 0),
  ASIA_ALMATY("Asia/Almaty", 6, 0),

  // UTC+7 (Southeast Asia)
  ASIA_JAKARTA("Asia/Jakarta", 7, 0),
  ASIA_BANGKOK("Asia/Bangkok", 7, 0),
  ASIA_HO_CHI_MINH("Asia/Ho_Chi_Minh", 7, 0),

  // UTC+8 (East Asia, Southeast Asia)
  ASIA_SHANGHAI("Asia/Shanghai", 8, 0),
  ASIA_HONG_KONG("Asia/Hong_Kong", 8, 0),
  ASIA_TAIPEI("Asia/Taipei", 8, 0),
  ASIA_MANILA("Asia/Manila", 8, 0),
  ASIA_SINGAPORE("Asia/Singapore", 8, 0),
  ASIA_KUALA_LUMPUR("Asia/Kuala_Lumpur", 8, 0),
  AUSTRALIA_PERTH("Australia/Perth", 8, 0),

  // UTC+9 (East Asia)
  ASIA_TOKYO("Asia/Tokyo", 9, 0),
  ASIA_SEOUL("Asia/Seoul", 9, 0),

  // UTC+9:30 (Australia)
  AUSTRALIA_ADELAIDE("Australia/Adelaide", 9, 30), // Has DST

  // UTC+10 (Australia, Pacific)
  AUSTRALIA_SYDNEY("Australia/Sydney", 10, 0), // Has DST (AEST/AEDT)
  AUSTRALIA_MELBOURNE("Australia/Melbourne", 10, 0), // Has DST
  AUSTRALIA_BRISBANE("Australia/Brisbane", 10, 0), // No DST

  // UTC+12 (New Zealand, Pacific)
  PACIFIC_AUCKLAND("Pacific/Auckland", 12, 0); // Has DST

  private final String postgresName;

  private final int offsetHours;

  private final int offsetMinutes;

  /** Get the offset as a string (e.g., "+07:00", "-05:00", "+05:30") */
  public String getOffsetString() {
    String sign = offsetHours >= 0 ? "+" : "-";
    int absHours = Math.abs(offsetHours);
    int absMinutes = Math.abs(offsetMinutes);
    return String.format("%s%02d:%02d", sign, absHours, absMinutes);
  }

  public ZoneId getZoneId() {
    return ZoneId.of(postgresName);
  }

  private static final Set<String> POSTGRES_NAMES =
      Arrays.stream(values()).map(PostgresTimezone::getPostgresName).collect(Collectors.toSet());

  public static boolean isValidPostgresTimezone(String timezoneName) {
    if (timezoneName == null || timezoneName.trim().isEmpty()) {
      return false;
    }

    return Arrays.stream(values()).anyMatch(tz -> tz.postgresName.equalsIgnoreCase(timezoneName));
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", postgresName, getOffsetString());
  }
}
