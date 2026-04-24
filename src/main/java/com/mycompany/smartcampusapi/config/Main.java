/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author marc
 */
package com.mycompany.smartcampusapi.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws Exception {
        ResourceConfig config = ResourceConfig.forApplicationClass(SmartCampusAPI.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);

        System.out.println("===========================================");
        System.out.println(" Smart Campus API started.");
        System.out.println(" Base: " + BASE_URI + "api/v1");
        System.out.println(" Press CTRL+C to stop.");
        System.out.println("===========================================");

        Thread.currentThread().join();
    }
}