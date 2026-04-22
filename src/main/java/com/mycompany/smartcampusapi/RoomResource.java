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

 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    
      private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms — list all rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.rooms.values());
        return Response.ok(roomList).build();
    }

    // POST /api/v1/rooms — create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required.\"}")
                    .build();
        }
        if (store.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A room with this ID already exists.\"}")
                    .build();
        }
        store.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — get a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found: " + roomId + "\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — delete a room (only if no sensors assigned)
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found: " + roomId + "\"}")
                    .build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room '" + roomId + 
                "' cannot be deleted — it still has " + 
                room.getSensorIds().size() + " sensor(s) assigned.");
        }

        store.rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}
