package com.sierrawireless.mangoh;

import static org.eclipse.leshan.client.object.Security.noSec;
import static org.eclipse.leshan.client.object.Security.noSecBootstap;
import static org.eclipse.leshan.client.object.Security.psk;
import static org.eclipse.leshan.client.object.Security.pskBootstrap;
import static org.eclipse.leshan.LwM2mId.DEVICE;
import static org.eclipse.leshan.LwM2mId.LOCATION;
import static org.eclipse.leshan.LwM2mId.SECURITY;
import static org.eclipse.leshan.LwM2mId.SERVER;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.util.Hex;

import com.sierrawireless.mangoh.objects.Device;
import com.sierrawireless.mangoh.objects.ProcessUtil;

public class MangohClient {

    private final static String DEFAULT_ENDPOINT = "mangoh-client";
    
     public static void main(final String[] args) {
            // Define options for command line tools
            Options options = new Options();

            options.addOption("h", "help", false, "Display help information.");
            options.addOption("n", true, String.format(
                    "Set the endpoint name of the Client. Default: the IMEI or '%s' if we can not find it.",
                    DEFAULT_ENDPOINT));
            options.addOption("b", false, "If present use bootstrap.");
            options.addOption("lh", true, "Set the local address of the Client. Default: any local address.");
            options.addOption("lp", true,
                    "Set the local port of the Client. Default: A valid port value is between 0 and 65535..");
            options.addOption("slh", true, "Set the secure local address of the Client. Default: any local address.");
            options.addOption("slp", true,
                    "Set the secure local port of the Client. Default: A valid port value is between 0 and 65535..");
            options.addOption("u", true, "Set the device management or bootstrap server URL. Default: localhost:5683.");
            options.addOption("i", true,
                    "Set the device management or bootstrap server PSK identity in ascii. Use none secure mode if not set.");
            options.addOption("p", true,
                    "Set the device management or bootstrap server Pre-Shared-Key in hexadecimal. Use none secure mode if not set.");
            HelpFormatter formatter = new HelpFormatter();

            // Parse arguments
            CommandLine cl = null;
            try {
                cl = new DefaultParser().parse(options, args);
            } catch (ParseException e) {
                System.err.println("Parsing failed.  Reason: " + e.getMessage());
                formatter.printHelp("LeshanClientExample", options);
                return;
            }

            // Print help
            if (cl.hasOption("help")) {
                formatter.printHelp("LeshanClientExample", options);
                return;
            }

            // Abort if unexpected options
            if (cl.getArgs().length > 0) {
                System.out.println("Unexpected option or arguments : " + cl.getArgList());
                formatter.printHelp("LeshanClientExample", options);
                return;
            }

            // Abort if we have not identity and key for psk.
            if ((cl.hasOption("i") && !cl.hasOption("p")) || !cl.hasOption("i") && cl.hasOption("p")) {
                System.out.println("You should precise identity and Pre-Shared-Key if you want to connect in PSK");
                formatter.printHelp("LeshanClientExample", options);
                return;
            }

            // Get endpoint name
            String endpoint;
            if (cl.hasOption("n")) {
                endpoint = cl.getOptionValue("n");
            } else {
                    endpoint =  ProcessUtil.command("cm", "info", "imei");
                    if (endpoint == null) {
                        endpoint = "mangoh-client";
                    }
            }
            // Get server URI
            String serverURI;
            if (cl.hasOption("u")) {
                if (cl.hasOption("i"))
                    serverURI = "coaps://" + cl.getOptionValue("u");
                else
                    serverURI = "coap://" + cl.getOptionValue("u");
            } else {
                if (cl.hasOption("i"))
                    serverURI = "coaps://localhost:5684";
                else
                    serverURI = "coaps://localhost:5683";
            }

            // get security info
            byte[] pskIdentity = null;
            byte[] pskKey = null;
            if (cl.hasOption("i") && cl.hasOption("p")) {
                pskIdentity = cl.getOptionValue("i").getBytes();
                pskKey = Hex.decodeHex(cl.getOptionValue("p").toCharArray());
            }

            // get local address
            String localAddress = null;
            int localPort = 0;
            if (cl.hasOption("lh")) {
                localAddress = cl.getOptionValue("lh");
            }
            if (cl.hasOption("lp")) {
                localPort = Integer.parseInt(cl.getOptionValue("lp"));
            }

            // get secure local address
            String secureLocalAddress = null;
            int secureLocalPort = 0;
            if (cl.hasOption("slh")) {
                secureLocalAddress = cl.getOptionValue("slh");
            }
            if (cl.hasOption("slp")) {
                secureLocalPort = Integer.parseInt(cl.getOptionValue("slp"));
            }

            createAndStartClient(endpoint, localAddress, localPort, secureLocalAddress, secureLocalPort, cl.hasOption("b"),
                    serverURI, pskIdentity, pskKey);
        }

        public static void createAndStartClient(String endpoint, String localAddress, int localPort,
                String secureLocalAddress, int secureLocalPort, boolean needBootstrap, String serverURI,
                byte[] pskIdentity, byte[] pskKey) {

            // Initialize object list
            ObjectsInitializer initializer = new ObjectsInitializer();
            if (needBootstrap) {
                if (pskIdentity == null)
                    initializer.setInstancesForObject(SECURITY, noSecBootstap(serverURI));
                else
                    initializer.setInstancesForObject(SECURITY, pskBootstrap(serverURI, pskIdentity, pskKey));
            } else {
                if (pskIdentity == null) {
                    initializer.setInstancesForObject(SECURITY, noSec(serverURI, 123));
                    initializer.setInstancesForObject(SERVER, new Server(123, 30, BindingMode.U, false));
                } else {
                    initializer.setInstancesForObject(SECURITY, psk(serverURI, 123, pskIdentity, pskKey));
                    initializer.setInstancesForObject(SERVER, new Server(123, 30, BindingMode.U, false));
                }
            }
            initializer.setClassForObject(3, Device.class);
            List<LwM2mObjectEnabler> enablers = initializer.create(SECURITY, SERVER, DEVICE);

            // Create client
            LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
            builder.setLocalAddress(localAddress, localPort);
            builder.setLocalSecureAddress(secureLocalAddress, secureLocalPort);
            builder.setObjects(enablers);
            final LeshanClient client = builder.build();

            // Start the client
            client.start();
        }
}
