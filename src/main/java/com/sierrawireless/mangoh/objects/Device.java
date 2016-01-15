package com.sierrawireless.mangoh.objects;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

/**
 * Object 3
 */
public class Device extends BaseInstanceEnabler {

    private final String manufacturer = "Sierra Wireless";
    private final String supportedBinding = "U";

    private String modelNumber;
    private String serialNumber;
    private String timezone = TimeZone.getDefault().getID();
    private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

    @Override
    public ReadResponse read(int resourceId) {
        System.out.println("Device: read on resource " + resourceId);

        switch (resourceId) {
        case 0: // manufacturer
            return ReadResponse.success(resourceId, manufacturer);

        case 1: // model number
            if (modelNumber == null) {
                modelNumber = this.getValueFromCommand("cm", "info", "device");
            }
            return ReadResponse.success(resourceId, modelNumber);

        case 2: // serial number
            if (serialNumber == null) {
                serialNumber = this.getValueFromCommand("cm", "info", "fsn");
            }
            return ReadResponse.success(resourceId, serialNumber);

        case 3: // firmware version
            return ReadResponse.success(resourceId, this.getValueFromCommand("cm", "info", "firmware"));

        case 6: // available power sources: USB
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceId,
                    Collections.singletonMap(0, 5L)));

        case 7: // voltage
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceId,
                    Collections.singletonMap(0, 5L)));

        case 8: // current
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceId,
                    Collections.singletonMap(0, 5L)));

        case 9: // battery level
            return ReadResponse.success(resourceId, 92);

        case 10: // memory free
            return ReadResponse.success(resourceId, (114 + new Random().nextInt(50)));

        case 11: // error codes
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceId,
                    Collections.singletonMap(0, 0L)));

        case 13: // time
            return ReadResponse.success(resourceId, new Date());

        case 14: // utc offset
            return ReadResponse.success(resourceId, utcOffset);

        case 15: // timezone
            return ReadResponse.success(resourceId, timezone);

        case 16: // supported binding and modes
            return ReadResponse.success(resourceId, supportedBinding);

        default:
            return super.read(resourceId);
        }
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {
        System.out.println("Device: write on resource " + resourceId);

        switch (resourceId) {

        case 14: // utc offset
            utcOffset = (String) value.getValue();
            fireResourcesChange(resourceId);
            return WriteResponse.success();

        case 15: // timezone
            timezone = (String) value.getValue();
            fireResourcesChange(resourceId);
            return WriteResponse.success();

        default:
            return super.write(resourceId, value);
        }
    }

    private String getValueFromCommand(String... command) {
        String result = ProcessUtil.command(command);
        if (result == null) {
            return "unknown";
        }
        return result;
    }

    @Override
    public ExecuteResponse execute(int resourceId, String params) {
        System.out.println("Device: exec on resource " + resourceId);

        if (resourceId == 4) { // reboot
            System.out.println("\n\n REBOOT ! \n\n");
            return ExecuteResponse.success();
        } else if (resourceId == 5) { // factory reset
            System.out.println("\n\n FACTORY RESET ! \n\n");
            return ExecuteResponse.success();
        } else {
            return super.execute(resourceId, params);
        }
    }

}
