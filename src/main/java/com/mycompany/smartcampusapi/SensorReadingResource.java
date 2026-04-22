/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marc
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
     private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        List<SensorReading> history = store.readings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Block if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is under MAINTENANCE and cannot accept readings.");
        }

        // Auto-generate id and timestamp if not provided
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save reading
        store.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // Update sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
    
}
