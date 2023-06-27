package com.example.enviar_ubicacion;

        import android.annotation.SuppressLint;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.app.admin.DevicePolicyManager;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.media.AudioManager;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Looper;
        import android.preference.PreferenceManager;
        import android.telephony.PhoneStateListener;
        import android.telephony.SmsManager;
        import android.telephony.TelephonyManager;
        import android.util.Log;
        import android.widget.Toast;

        import androidx.annotation.RequiresApi;
        import androidx.core.app.ActivityCompat;
        import androidx.core.app.NotificationCompat;

public class ServiceSegundoPlano extends Service  {

    public static String telefono;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    private CallStateListener callStateListener;
    // Objeto de proveedor de ubicación
    private LocationProvider locationProvider;
    public double latitudeG;
    public double longitudG;

    private AudioManager audioManager;




    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();


        // Inicializar y registrar el CallStateListener
        callStateListener = new CallStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        }

        // Inicializar el proveedor de ubicación
        locationProvider = new LocationProvider(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "servico iniciado", Toast.LENGTH_SHORT).show();




        createNotificationChannel();

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("LLamadasGPS")
                .setContentText("Rastreando llamada")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        // Abrir la actividad principal al hacer clic en la notificación
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        // Iniciar el servicio en primer plano con la notificación
        startForeground(NOTIFICATION_ID, builder.build());


        // Obtener la ubicación actual
        Location currentLocation = locationProvider.getCurrentLocation();
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            latitudeG = latitude;
            longitudG = longitude;
            Toast.makeText(this, "Latitud: " + latitude + ", Longitud: " + longitude, Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Foreground Service Channel";
            String channelDescription = "Channel for foreground service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Liberar recursos y finalizar el servicio
        // Detener la escucha del CallStateListener
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        }

        // Detener la actualización de ubicación si es necesario
        locationProvider.stopLocationUpdates();
    }

    public  void llamada(String numeroAllamar){
        Toast.makeText(getApplicationContext(), "Llamando", Toast.LENGTH_LONG).show();
        String phoneNumber = numeroAllamar; // Número de teléfono al que deseas llamar
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            Toast.makeText(this, "No tiene permiso para llamar", Toast.LENGTH_SHORT).show();
        }
    }

    public void BloquerPantallaMetodo() {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, YourDeviceAdminReceiver.class);

        if (policyManager.isAdminActive(adminComponent)) {
                policyManager.lockNow();

        } else {
            Toast.makeText(this, "Error bloqueo", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRingerModeSilent() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        // Silenciar el teléfono
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        Toast.makeText(this, "silencio activado", Toast.LENGTH_SHORT).show();
    }

    public void Enviarsms(String incomingNumber){
        try {
            // Envío de mensaje SMS automático
            SmsManager smsManager = SmsManager.getDefault();
            String mensaje = "Hola, no puedo contestar mira mi ubicacion: ";
            mensaje += "https://maps.google.com/?q=" + latitudeG + "," + longitudG;
            smsManager.sendTextMessage(incomingNumber, null, mensaje, null, null);

            Toast.makeText(getApplicationContext(), "SMS enviado: "+incomingNumber, Toast.LENGTH_LONG).show();
            Log.d("smserror","SMS enviado: "+incomingNumber);

        }catch (Exception e){
            Log.d("smserror",e.toString());
            Toast.makeText(getApplicationContext(), "Numero no valido.", Toast.LENGTH_LONG).show();
        }

    }


    public class CallStateListener extends PhoneStateListener {

        Handler handler = new Handler(Looper.getMainLooper());
        private boolean incomingCall = false;
        private int ringCount = 0;

        // Obtener el número de teléfono guardado en las preferencias compartidas
        @SuppressWarnings("deprecation")
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tel = sharedPreferences.getString("telefono", "");

        @SuppressWarnings("deprecation")
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // Llamada entrante
                    Toast.makeText(ServiceSegundoPlano.this, "Numero entrante: "+incomingNumber, Toast.LENGTH_SHORT).show();

                    if (incomingNumber.equals(tel)) {
                        incomingCall = true;
                        ringCount = 1;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // Llamada en progreso (contestada)
                    if (incomingCall) {
                        incomingCall = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:

                    // Llamada finalizada (colgada)
                    if (incomingCall && ringCount == 1) {
                        //Toast.makeText(getApplicationContext(), "llamada perdida:"+incomingNumber, Toast.LENGTH_SHORT).show();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Toast.makeText(getApplicationContext(), "silencio", Toast.LENGTH_LONG).show();
                                    //Thread.sleep(2000);// Retraso de 4 segundo
                                    setRingerModeSilent();  // Cambiar al modo de silencio
                                    Log.d("handlerrun","1. silenciado: "+incomingNumber);

                                    Toast.makeText(getApplicationContext(), "envio sms 4s", Toast.LENGTH_LONG).show();
                                    Thread.sleep(4000); // Retraso de 4 segundo
                                    Enviarsms(incomingNumber);// SMS ENVIADO
                                    Log.d("handlerrun","2. mensaje enviado: "+incomingNumber);

                                    Thread.sleep(4000);// Retraso de 4 segundo
                                    llamada(incomingNumber); // Realizar la llamada
                                    Log.d("handlerrun","3. llamando: "+incomingNumber);

                                    //Toast.makeText(getApplicationContext(), "bloqueo de telefono 4s", Toast.LENGTH_LONG).show();
                                    Thread.sleep(4000); // Retraso de 4 segundo
                                    BloquerPantallaMetodo(); // Bloquear la pantalla
                                    Log.d("handlerrun","4. bloqueado: "+incomingNumber);

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error de procesos", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        });



                    }
                    incomingCall = false;
                    ringCount = 0;
                    break;
            }
        }
    }




}

