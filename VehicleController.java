package com.vehiclerental.vehicle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vehiclerental.common.RequestData;

/**
 * MEMBER 2 - REST endpoints used by the vehicle pages.
 *
 *  POST   /api/vehicles                    add vehicle      (Create)
 *  GET    /api/vehicles?q=&type=&status=   search / list    (Read)
 *  GET    /api/vehicles/{id}               one vehicle      (Read)
 *  GET    /api/vehicles/{id}/quote?days=   price quote      (Read - polymorphic pricing)
 *  GET    /api/vehicles/stats              fleet numbers    (Read)
 *  PUT    /api/vehicles/{id}               edit vehicle     (Update)
 *  PATCH  /api/vehicles/{id}/status        change status    (Update)
 *  DELETE /api/vehicles/{id}               remove vehicle   (Delete)
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<Vehicle> add(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.add(new RequestData(body)));
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> search(@RequestParam(value = "q", required = false) String q,
                                                @RequestParam(value = "type", required = false) String type,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "branchId", required = false) String branchId,
                                                @RequestParam(value = "maxRate", required = false) Double maxRate) {
        return ResponseEntity.ok(vehicleService.search(q, type, status, branchId, maxRate));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(vehicleService.getStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getOne(@PathVariable("id") String id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @GetMapping("/{id}/quote")
    public ResponseEntity<Map<String, Object>> quote(@PathVariable("id") String id,
                                                     @RequestParam(value = "days", defaultValue = "1") int days,
                                                     @RequestParam(value = "discount", defaultValue = "0") double discount) {
        return ResponseEntity.ok(vehicleService.quote(id, days, discount));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable("id") String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(vehicleService.update(id, new RequestData(body)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Vehicle> changeStatus(@PathVariable("id") String id, @RequestBody Map<String, Object> body) {
        VehicleStatus status = VehicleStatus.fromText(new RequestData(body).getString("status"));
        return ResponseEntity.ok(vehicleService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") String id) {
        vehicleService.delete(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Vehicle " + id + " removed from the fleet");
        return ResponseEntity.ok(result);
    }
}
