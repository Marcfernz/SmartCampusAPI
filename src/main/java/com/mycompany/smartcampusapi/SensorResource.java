package com.mycompany.smartcampusapi;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(store.sensors.values());

        if (type != null) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : list) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }

        return Response.ok(list).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null) {
            return Response.status(400)
                    .entity("{\"error\": \"Sensor ID is required.\"}")
                    .build();
        }

        if (store.sensors.containsKey(sensor.getId())) {
            return Response.status(409)
                    .entity("{\"error\": \"Sensor already exists.\"}")
                    .build();
        }

        if (!store.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room " + sensor.getRoomId() + " does not exist.");
        }

        store.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        store.sensors.put(sensor.getId(), sensor);

        return Response.status(201).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\": \"Sensor not found.\"}")
                    .build();
        }

        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}