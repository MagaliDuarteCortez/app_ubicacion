package com.example.enviar_ubicacion;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class YourDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}
