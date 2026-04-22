/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.smartcampusapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author marc
 */


@ApplicationPath("/api/v1")
public class SmartCampusAPI extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(RoomResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(DiscoverResource.class);
        classes.add(SensorResource.class);
        classes.add(SensorReadingResource.class);
classes.add(SensorUnavailableExceptionMapper.class);
classes.add(LinkedResourceNotFoundExceptionMapper.class);
classes.add(GlobalExceptionMapper.class);
classes.add(LoggingFilter.class);
        return classes;
    }
}
