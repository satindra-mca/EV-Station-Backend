package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.BEEImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class BEEImportController {

    private final BEEImportService beeImportService;

    public BEEImportController(BEEImportService beeImportService) {
        this.beeImportService = beeImportService;
    }

    @PostMapping("/bee")
    public ResponseEntity<String> importBEEData() {

        int importedRows = beeImportService.importBEEData();

        return ResponseEntity.ok(
                "BEE data imported successfully. Rows processed: " + importedRows
        );
    }
}
