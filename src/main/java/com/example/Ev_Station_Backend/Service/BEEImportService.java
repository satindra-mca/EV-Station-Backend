package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Repository.ChargerRepository;
import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.ChargingStation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BEEImportService {

    private final ChargingStationRepository chargingStationRepository;
    private final ChargerRepository chargerRepository;

    public BEEImportService(
            ChargingStationRepository chargingStationRepository,
            ChargerRepository chargerRepository) {

        this.chargingStationRepository = chargingStationRepository;
        this.chargerRepository = chargerRepository;
    }

    @Transactional
    public int importBEEData() {

        int importedRows = 0;

        try {

            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "data/Gujarat_EV_Charging_Stations_BEE_Cleaned.csv"
                    );

            if (inputStream == null) {
                throw new RuntimeException("BEE CSV file not found");
            }

            Reader reader = new InputStreamReader(
                    inputStream,
                    StandardCharsets.UTF_8
            );

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : csvParser) {

                // ---------------------------------
                // 1. Read Station Data
                // ---------------------------------

                String cpoName = record.get("cpo_name");

                String location = cleanLocation(
                        record.get("location")
                );

                Double latitude = parseDouble(
                        record.get("latitude")
                );

                Double longitude = parseDouble(
                        record.get("longitude")
                );

                // ---------------------------------
                // 2. Find Existing Station
                // ---------------------------------

                Optional<ChargingStation> existingStation =
                        chargingStationRepository
                                .findByCpoNameAndLocationAndLatitudeAndLongitude(
                                        cpoName,
                                        location,
                                        latitude,
                                        longitude
                                );

                ChargingStation station;

                // ---------------------------------
                // 3. Create Station if Not Exists
                // ---------------------------------

                if (existingStation.isPresent()) {

                    station = existingStation.get();

                } else {

                    station = new ChargingStation();

                    station.setCpoName(cpoName);

                    station.setGovtPrivate(
                            record.get("govt_private")
                    );

                    station.setState(
                            record.get("state")
                    );

                    station.setDistrict(
                            record.get("district")
                    );

                    station.setCityVillage(
                            record.get("city_village")
                    );

                    station.setLocation(location);

                    station.setLatitude(latitude);

                    station.setLongitude(longitude);

                    station.setSource(
                            record.get("source")
                    );

                    station.setStatus("ACTIVE");

                    station.setCreatedAt(
                            LocalDateTime.now()
                    );

                    station.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    station =
                            chargingStationRepository.save(station);
                }

                // ---------------------------------
                // 4. Read BEE Source Index
                // ---------------------------------

                Long beeSourceIndex = parseLong(
                        record.get(0)
                );

                // ---------------------------------
                // 5. Check Existing Charger
                // ---------------------------------

                Optional<Charger> existingCharger =
                        chargerRepository.findByBeeSourceIndex(
                                beeSourceIndex
                        );

                if (existingCharger.isPresent()) {

                    // Charger already imported
                    continue;
                }

                // ---------------------------------
                // 6. Create Charger
                // ---------------------------------

                Charger charger = new Charger();

                charger.setBeeSourceIndex(
                        beeSourceIndex
                );

                charger.setChargerType(
                        record.get("charger_type")
                );

                charger.setChargerRating(
                        record.get("charger_rating")
                );

                charger.setConnectorRating(
                        record.get("connector_rating")
                );

                charger.setNoOfConnector(
                        parseInteger(
                                record.get("no_of_connector")
                        )
                );

                // ---------------------------------
                // 7. Link Charger to Station
                // ---------------------------------

                charger.setChargingStation(station);

                // ---------------------------------
                // 8. Save Charger
                // ---------------------------------

                chargerRepository.save(charger);

                importedRows++;
            }

            csvParser.close();
            reader.close();

            return importedRows;

        } catch (Exception e) {

            throw new RuntimeException(
                    "BEE CSV import failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // ---------------------------------
    // Location Cleaning
    // ---------------------------------

    private String cleanLocation(String location) {

        if (location == null) {
            return null;
        }

        location = location.trim();

        /*
         * Some BEE records contain a valid Gujarat
         * address followed by unrelated Haryana
         * addresses.
         *
         * Keep only the Gujarat portion.
         */

        if (location.length() > 500) {

            int gujaratIndex =
                    location.toLowerCase()
                            .indexOf("gujarat");

            if (gujaratIndex != -1) {

                location = location.substring(
                        0,
                        gujaratIndex + "Gujarat".length()
                ).trim();
            }
        }

        // Safety check
        if (location.length() > 500) {

            throw new RuntimeException(
                    "Location still exceeds 500 characters: "
                            + location.length()
            );
        }

        return location;
    }

    // ---------------------------------
    // Parse Double
    // ---------------------------------

    private Double parseDouble(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    // ---------------------------------
    // Parse Integer
    // ---------------------------------

    private Integer parseInteger(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    // ---------------------------------
    // Parse Long
    // ---------------------------------

    private Long parseLong(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }
}
