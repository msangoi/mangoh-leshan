package com.sierrawireless.mangoh;

import java.util.List;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.util.LwM2mId;
import org.eclipse.leshan.core.request.BindingMode;

import com.sierrawireless.mangoh.objects.Device;
import com.sierrawireless.mangoh.objects.ProcessUtil;

public class MangohClient {

    /*
     * To build: mvn assembly:assembly -DdescriptorId=jar-with-dependencies
     * 
     * To use: java -jar target/mangoh-leshan-*-jar-with-dependencies.jar leshan.eclipse.org 5683
     */
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.out
                    .println("Usage:\njava -jar target/mangoh-leshan-*-jar-with-dependencies.jar ServerIP ServerPort");
        } else {
            new MangohClient(args[0], Integer.parseInt(args[1]));
        }
    }

    public MangohClient(final String serverHostName, final int serverPort) {
        // get Endpoint Name
        String endpoint = ProcessUtil.command("cm", "info", "imei");
        if (endpoint == null) {
            endpoint = "mangoh-client";
        }
        System.out.println("client endpoint: " + endpoint);

        // initialize objects list
        ObjectsInitializer initializer = new ObjectsInitializer();
        initializer.setInstancesForObject(LwM2mId.SECURITY_ID,
                Security.noSec("coap://" + serverHostName + ":" + serverPort, 123));
        initializer.setInstancesForObject(LwM2mId.SERVER_ID, new Server(123, 30, BindingMode.U, false));
        initializer.setClassForObject(3, Device.class);
        List<LwM2mObjectEnabler> enablers = initializer.createMandatory();

        // create client
        LeshanClientBuilder builder = new LeshanClientBuilder();
        builder.setEndpoint(endpoint);
        builder.setObjects(enablers);
        final LeshanClient client = builder.build();

        // start the client
        client.start();
        client.bootstrap();
    }

}
