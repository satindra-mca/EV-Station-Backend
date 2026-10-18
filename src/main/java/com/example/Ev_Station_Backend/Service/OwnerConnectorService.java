
package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.Role;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.ChargerRepository;
import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import com.example.Ev_Station_Backend.Repository.ConnectorRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.ConnectorRequest;
import com.example.Ev_Station_Backend.dto.OwnerConnectorResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.entity.Connector;
import com.example.Ev_Station_Backend.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OwnerConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public OwnerConnectorService(
            ConnectorRepository connectorRepository,
            ChargerRepository chargerRepository,
            ChargingStationRepository chargingStationRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository) {

        this.connectorRepository = connectorRepository;
        this.chargerRepository = chargerRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<OwnerConnectorResponse> getOwnerConnectors(String email) {
        ChargingStation station = getOwnerStation(email);

        return connectorRepository
                .findAllByChargerChargingStationId(station.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OwnerConnectorResponse getOwnerConnectorById(
            String email, Long connectorId) {

        ChargingStation station = getOwnerStation(email);
        Connector connector = getOwnedConnector(connectorId, station);

        return convertToResponse(connector);
    }

    @Transactional
    public OwnerConnectorResponse createConnector(
            String email, ConnectorRequest request) {

        ChargingStation station = getOwnerStation(email);
        Charger charger = getOwnedCharger(request.getChargerId(), station);

        Connector connector = new Connector();
        connector.setConnectorNumber(request.getConnectorNumber());
        connector.setStatus(request.getStatus());
        connector.setCharger(charger);

        Connector savedConnector = connectorRepository.save(connector);

        return convertToResponse(savedConnector);
    }

    @Transactional
    public OwnerConnectorResponse updateConnector(
            String email, Long connectorId, ConnectorRequest request) {

        ChargingStation station = getOwnerStation(email);
        Connector connector = getOwnedConnector(connectorId, station);
        Charger charger = getOwnedCharger(request.getChargerId(), station);

        connector.setConnectorNumber(request.getConnectorNumber());
        connector.setStatus(request.getStatus());
        connector.setCharger(charger);

        Connector updatedConnector = connectorRepository.save(connector);

        return convertToResponse(updatedConnector);
    }

    @Transactional
    public void deleteConnector(String email, Long connectorId) {

        ChargingStation station = getOwnerStation(email);
        Connector connector = getOwnedConnector(connectorId, station);

        if (bookingRepository.existsByConnectorId(connectorId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete connector because booking records exist");
        }

        connectorRepository.delete(connector);
    }

    private ChargingStation getOwnerStation(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Owner account not found"));

        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owners can manage connectors");
        }

        return chargingStationRepository.findByOwner_Id(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No charging station assigned to this owner"));
    }

    private Charger getOwnedCharger(
            Long chargerId, ChargingStation station) {

        if (chargerId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Charger ID is required");
        }

        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Charger not found"));

        if (charger.getChargingStation() == null
                || !station.getId().equals(
                        charger.getChargingStation().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this charger");
        }

        return charger;
    }

    private Connector getOwnedConnector(
            Long connectorId, ChargingStation station) {

        Connector connector = connectorRepository.findById(connectorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Connector not found"));

        Charger charger = connector.getCharger();

        if (charger == null
                || charger.getChargingStation() == null
                || !station.getId().equals(
                        charger.getChargingStation().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this connector");
        }

        return connector;
    }

    private OwnerConnectorResponse convertToResponse(
            Connector connector) {

        OwnerConnectorResponse response = new OwnerConnectorResponse();
        response.setId(connector.getId());
        response.setConnectorNumber(connector.getConnectorNumber());
        response.setStatus(connector.getStatus());
        response.setChargerId(connector.getCharger().getId());

        return response;
    }
}
