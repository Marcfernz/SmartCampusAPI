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
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors — list all sensors, optional ?type= filter
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.sensors.values());

        if (type != null && !type.isBlank()) {
            result.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }

        return Response.ok(result).build();
    }

    // POST /api/v1/sensors — register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sensor ID is required.\"}")
                    .build();
        }

        if (store.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A sensor with this ID already exists.\"}")
                    .build();
        }

        // Verify the roomId exists
        if (sensor.getRoomId() == null || !store.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room '" + sensor.getRoomId() + "' does not exist. Cannot register sensor.");
        }

        // Add sensor to the room's sensorIds list
        store.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        store.sensors.put(sensor.getId(), sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId} — get a specific sensor
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }
    @Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
    
}
