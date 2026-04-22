/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author marc
 */
public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    public final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();


    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }
}
