package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.Role;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.ChargerRepository;
import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.ChargerRequest;
import com.example.Ev_Station_Backend.dto.ChargerResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OwnerChargerService {

    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public OwnerChargerService(
            ChargerRepository chargerRepository,
            ChargingStationRepository chargingStationRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository) {

        this.chargerRepository = chargerRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // Get all chargers belonging to the logged-in owner's station
    @Transactional(readOnly = true)
    public List<ChargerResponse> getOwnerChargers(String email) {

        ChargingStation station = getOwnerStation(email);

        return chargerRepository
                .findAllByChargingStationId(station.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get one charger after verifying station ownership
    @Transactional(readOnly = true)
    public ChargerResponse getOwnerChargerById(
            String email, Long chargerId) {

        ChargingStation station = getOwnerStation(email);
        Charger charger = getOwnedCharger(chargerId, station);

        return convertToResponse(charger);
    }

    // Create a charger in the owner's assigned station
    @Transactional
    public ChargerResponse createCharger(
            String email, ChargerRequest request) {

        ChargingStation station = getOwnerStation(email);

        Charger charger = new Charger();
        charger.setChargerType(request.getChargerType());
        charger.setChargerRating(request.getChargerRating());
        charger.setConnectorRating(request.getConnectorRating());
        charger.setNoOfConnector(request.getNoOfConnector());
        charger.setPricePerKwh(java.math.BigDecimal.ZERO);

        // Never trust a station ID supplied by the client
        charger.setChargingStation(station);

        Charger savedCharger = chargerRepository.save(charger);

        return convertToResponse(savedCharger);
    }

    // Update a charger only if it belongs to the owner's station
    @Transactional
    public ChargerResponse updateCharger(
            String email,
            Long chargerId,
            ChargerRequest request) {

        ChargingStation station = getOwnerStation(email);
        Charger charger = getOwnedCharger(chargerId, station);

        charger.setChargerType(request.getChargerType());
        charger.setChargerRating(request.getChargerRating());
        charger.setConnectorRating(request.getConnectorRating());
        charger.setNoOfConnector(request.getNoOfConnector());

        // Keep the existing station assignment unchanged
        Charger updatedCharger = chargerRepository.save(charger);

        return convertToResponse(updatedCharger);
    }

    // Delete only when no booking references this charger's connectors
    @Transactional
    public void deleteCharger(String email, Long chargerId) {

        ChargingStation station = getOwnerStation(email);
        Charger charger = getOwnedCharger(chargerId, station);

        if (bookingRepository.existsByConnectorChargerId(chargerId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete charger because booking records exist"
            );
        }

        chargerRepository.delete(charger);
    }

    // Verify the logged-in user is an owner with an assigned station
    private ChargingStation getOwnerStation(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Owner account not found"
                ));

        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owners can manage chargers"
            );
        }

        return chargingStationRepository.findByOwner_Id(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No charging station assigned to this owner"
                ));
    }

    // Prevent access to chargers belonging to another station
    private Charger getOwnedCharger(
            Long chargerId, ChargingStation station) {

        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Charger not found"
                ));

        if (charger.getChargingStation() == null
                || !station.getId().equals(
                        charger.getChargingStation().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this charger"
            );
        }

        return charger;
    }

    // Convert entity to response DTO
    private ChargerResponse convertToResponse(Charger charger) {

        ChargerResponse response = new ChargerResponse();

        response.setId(charger.getId());
        response.setChargerType(charger.getChargerType());
        response.setChargerRating(charger.getChargerRating());
        response.setConnectorRating(charger.getConnectorRating());
        response.setNoOfConnector(charger.getNoOfConnector());
        response.setChargingStationId(
                charger.getChargingStation().getId()
        );

        return response;
    }
}