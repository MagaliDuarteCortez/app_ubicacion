package com.example.enviar_ubicacion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {

    private Button btnguardar,btnpermisos,btn_permisosADMIN;
    private EditText NumeroEDIT;

    private Boolean PermisosOK = false;

    private static final int PERMISSION_REQUEST_CODE_ACTION = 1;
    private static final int REQUEST_CODE_PERMISOS = 1;

    private static final int PERMISSION_REQUEST_CODE_POST = 1;



    String[] permisos = {
            android.Manifest.permission.READ_CALL_LOG, //1
            android.Manifest.permission.READ_PHONE_STATE, //1
            android.Manifest.permission.SEND_SMS, //1
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION, //1
            android.Manifest.permission.CALL_PHONE //1
    };



    String[] permisos2 = {
            android.Manifest.permission.READ_CALL_LOG, //1
            android.Manifest.permission.READ_PHONE_STATE, //1
            android.Manifest.permission.SEND_SMS, //1
            android.Manifest.permission.ACCESS_FINE_LOCATION, //1
            android.Manifest.permission.CALL_PHONE //1
    };

    private AudioManager audioManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnguardar = findViewById(R.id.btn_guardar);
        btnpermisos = findViewById(R.id.btn_permisos);

        btn_permisosADMIN = findViewById(R.id.btn_permisosADMIN);
        NumeroEDIT = findViewById(R.id.Numeroeditext);

        btnguardar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PermisosOK==true){
                            guardar_numero();

                        } else{
                            //por si no se solicitaron
                            //requestPermissions(permisos, REQUEST_CODE_PERMISOS);
                        }



                    }

        });
        btnpermisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "::"+PermisosOK, Toast.LENGTH_SHORT).show();
                permisoalertas();
               // solicitarpermisoNOTI();
                XIAOMI();

            }

        });

        if (Build.VERSION.SDK_INT <33) {

            // No solicitar el permiso android.Manifest.permission.POST_NOTIFICATIONS
            // Solicitar permisos al iniciar la actividad
           requestPermissions(permisos2, REQUEST_CODE_PERMISOS);
        } else {
            // Solicitar todos los permisos, incluido android.Manifest.permission.POST_NOTIFICATIONS
           requestPermissions( permisos, REQUEST_CODE_PERMISOS);

        }

        btn_permisosADMIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solicitar todos los permisos, incluido
                PermisosPantalla();
                permisoSonido(MainActivity.this.getApplicationContext());

            }

        });



    }


    //guardar numero en sharedpreferrences
    public void guardar_numero(){
        String numeroEDITst = NumeroEDIT.getText().toString().trim();
        if (esNumero(numeroEDITst) && !numeroEDITst.isEmpty()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().putString("telefono", numeroEDITst).apply();
            Intent INTENTO = new Intent(MainActivity.this, ServiceSegundoPlano.class);
            startService(INTENTO);
            //  segundo plano
            moveTaskToBack(true);

        } else {
            Toast.makeText(MainActivity.this, "Error de Numero", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean esNumero(String texto) {
        try {
            Long.parseLong(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    //enviar mensaje sms automatico
    public void send_sms(){
        String telefono = NumeroEDIT.getText().toString().trim();
        String message = "Hola, Mi ubicacion es: ";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(telefono, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS enviado.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al enviar el SMS.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISOS) {
            boolean todosConcedidos = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    todosConcedidos = false;

                    // Verificar si el usuario seleccionó "No volver a preguntar"
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        // Al menos un permiso fue denegado y "No volver a preguntar" fue seleccionado
                        // Mostrar un mensaje explicando la necesidad de los permisos y un botón para abrir la configuración de la aplicación
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permisos denegados");
                        builder.setMessage("Se requieren los permisos para utilizar todas las funciones de la aplicación. Por favor, habilite los permisos en la configuración de la aplicación.");
                        builder.setPositiveButton("Abrir configuración", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Abrir la configuración de la aplicación
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        builder.show();
                    }
                }
            }

            if (todosConcedidos) {
                // Todos los permisos fueron concedidos
                PermisosOK = true;
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                // Al menos un permiso fue denegado, pero "No volver a preguntar" no fue seleccionado
                // Puedes mostrar un mensaje o realizar alguna otra acción
                Toast.makeText(this, "Al menos un permiso fue denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void solicitarpermisoNOTI() {
        // Verificar si se tienen los permisos necesarios
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no ha sido concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE_POST);
        } else {
            // Si el permiso ya ha sido concedido, continuar con la lógica de mostrar la notificación
        }

    }


    @SuppressWarnings("deprecation")
    private void permisoalertas() {
        if (!Settings.canDrawOverlays(this)) {
            // El permiso no está concedido, debes solicitarlo
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_ACTION);
        } else {
            // El permiso ya está concedido, puedes continuar con las acciones necesarias

        }
    }


    //SOLO PARA DISPOSITIVOS XIAOMI
    public void XIAOMI() {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsTabActivity"));

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        if (!activities.isEmpty()) {
            startActivity(intent);
        } else {

        }


    }

    public void PermisosPantalla() {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, YourDeviceAdminReceiver.class);

        if (policyManager.isAdminActive(adminComponent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //policyManager.lockNow();
                Toast.makeText(this, "permiso pantalla ok", Toast.LENGTH_SHORT).show();
            } else {
                // Para versiones anteriores a Android Oreo, abre la configuración de pantalla para que el usuario pueda apagarla manualmente.
                Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Explanation about why this permission is needed");
            startActivity(intent);
        }
    }

    public void permisoSonido(Context context){

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }else{
            Toast.makeText(context, "Permiso sonido ok", Toast.LENGTH_SHORT).show();
        }
    }


}

