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
    public ReadResponse read(int resourceid) {
        switch (resourceid) {
        case 0: // manufacturer
            return ReadResponse.success(resourceid, manufacturer);

        case 1: // model number
            if (modelNumber == null) {
                modelNumber = this.getValueFromCommand("cm", "info", "device");
            }
            return ReadResponse.success(resourceid, modelNumber);

        case 2: // serial number
            if (serialNumber == null) {
                serialNumber = this.getValueFromCommand("cm", "info", "fsn");
            }
            return ReadResponse.success(resourceid, serialNumber);

        case 3: // firmware version
            return ReadResponse.success(resourceid, this.getValueFromCommand("cm", "info", "firmware"));

        case 6: // available power sources: USB
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceid,
                    Collections.singletonMap(0, 5L)));

        case 7: // voltage
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceid,
                    Collections.singletonMap(0, 5L)));

        case 8: // current
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceid,
                    Collections.singletonMap(0, 5L)));

        case 9: // battery level
            return ReadResponse.success(resourceid, 92);

        case 10: // memory free
            return ReadResponse.success(resourceid, (114 + new Random().nextInt(50)));

        case 11: // error codes
            return ReadResponse.success(LwM2mMultipleResource.newIntegerResource(resourceid,
                    Collections.singletonMap(0, 0L)));

        case 13: // time
            return ReadResponse.success(resourceid, new Date());

        case 14: // utc offset
            return ReadResponse.success(resourceid, utcOffset);

        case 15: // timezone
            return ReadResponse.success(resourceid, timezone);

        case 16: // supported binding and modes
            return ReadResponse.success(resourceid, supportedBinding);

        default:
            return super.read(resourceid);
        }
    }

    @Override
    public WriteResponse write(int resourceid, LwM2mResource value) {

        switch (resourceid) {

        case 14: // utc offset
            utcOffset = (String) value.getValue();
            fireResourcesChange(resourceid);
            return WriteResponse.success();

        case 15: // timezone
            timezone = (String) value.getValue();
            fireResourcesChange(resourceid);
            return WriteResponse.success();

        default:
            return super.write(resourceid, value);
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
    public ExecuteResponse execute(int resourceid, String params) {

        if (resourceid == 4) { // reboot

            // TODO reboot

            return ExecuteResponse.success();

        } else {
            return super.execute(resourceid, params);
        }
    }

}
