package com.example.enviar_ubicacion;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationProvider {

    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // 10 segundos
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10.0f; // 10 metros

    private Context context;
    private LocationManager locationManager;

    public LocationProvider(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public Location getCurrentLocation() {
        // Comprobar si se concedieron los permisos de ubicación
        if (PermissionUtils.checkLocationPermission(context)) {
            try {
                // Obtener la última ubicación conocida
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    return lastKnownLocation;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void startLocationUpdates() {
        // Comprobar si se concedieron los permisos de ubicación
        if (PermissionUtils.checkLocationPermission(context)) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // La ubicación ha cambiado
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
