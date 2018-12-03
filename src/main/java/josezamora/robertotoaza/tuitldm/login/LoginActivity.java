/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Actividad de Login
 */

package josezamora.robertotoaza.tuitldm.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import josezamora.robertotoaza.tuitldm.R;
import josezamora.robertotoaza.tuitldm.chat.ChatsActivity;
import josezamora.robertotoaza.tuitldm.clases.Conexion;
import josezamora.robertotoaza.tuitldm.clases.Mensaje;
import josezamora.robertotoaza.tuitldm.clases.Usuario;

public class LoginActivity extends AppCompatActivity {

    static final String USUARIOS = "Usuarios";
    static final String CONEXIONES ="Conexiones";
    static final String MENSAJES = "Mensajes";
    static final String USUARIO_ACTUAL ="u_actual";
    static final String PREFERENCIAS = "Preferencias";

    Usuario usuario;

    // Elementos de Firebase.
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usuariosRef = db.collection(USUARIOS);

    // Elementos de la interfaz.
    private EditText editTextNick;
    private EditText editTextPass;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_foreground);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(this,R.style.estiloFuenteActionBar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);


        usuario = new Usuario();

        cargarPreferencias();

        editTextNick = findViewById(R.id.editText_nickLogin);
        editTextPass = findViewById(R.id.editText_passLogin);

        Button btnEntrar = findViewById(R.id.button_entrarLogin);
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickEntrar();
            }
        });

        Button btnCrear = findViewById(R.id.button_crearLogin);
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCrear();
            }
        });
    }

    private void cargarPreferencias() {

        preferences = getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        String user = preferences.getString("user", "0");
        String pass = preferences.getString("pass", "0");

        assert user != null;
        assert pass != null;
        if(!user.equals("0") && !pass.equals("0"))
            entrarToFirebase(user, pass);
    }

    private void clickCrear(){
        if(isOnline(this)) {
            if (editTextNick.getText().toString().equals("") || editTextNick.getText().toString().equals("")) {
                Toast.makeText(this, "Hay campos vacíos", Toast.LENGTH_SHORT).show();
                editTextNick.setText("");
                editTextPass.setText("");
            } else crearToFirebase(editTextNick.getText().toString());
        }
        else Toast.makeText(this, "No tienes acceso a internet", Toast.LENGTH_SHORT).show();
    }

    private void clickEntrar(){
        if(isOnline(this)){
            if(editTextNick.getText().toString().equals("") || editTextNick.getText().toString().equals("") ){
                Toast.makeText(this, "Hay campos vacíos", Toast.LENGTH_SHORT).show();
                editTextNick.setText("");
                editTextPass.setText("");
            }
            else entrarToFirebase(editTextNick.getText().toString(), editTextPass.getText().toString());
        }
        else Toast.makeText(this, "No tienes acceso a internet", Toast.LENGTH_SHORT).show();
    }

    private void abrirInfoLogin() {

        LayoutInflater li = LayoutInflater.from(LoginActivity.this);
        AlertDialog.Builder builderInfo = new AlertDialog.Builder(LoginActivity.this);

        @SuppressLint("InflateParams")
        View viewInfoLogin = li.inflate(R.layout.card_info_login, null);

        builderInfo.setView(viewInfoLogin);

        AlertDialog alertDialog = builderInfo.create();
        alertDialog.show();
    }

    public void crearToFirebase (final String nick ){
        usuariosRef.document(nick).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            Toast.makeText(LoginActivity.this, "Ya existe un usario con ese nick.", Toast.LENGTH_SHORT).show();
                            editTextNick.setText("");
                            editTextPass.setText("");
                        }
                        else{
                            // detalles del usuario.
                            String nick = editTextNick.getText().toString();
                            String pass = editTextPass.getText().toString();
                            String realName ="";
                            String realLastName="";
                            String urlUsuarioImgPerfil="";
                            String urlUsuarioDocument=USUARIOS+"/"+nick;

                            usuario = new Usuario (nick, pass, realName, realLastName, urlUsuarioDocument, urlUsuarioImgPerfil);
                            db.document(urlUsuarioDocument).set(usuario);

                            // detalles de la conexión.
                            String idConexion = Conexion.crearConexion(nick, nick);
                            String nickUsuarioA = usuario.getNick();
                            String nickUsuarioB = usuario.getNick();
                            String urlConexionDocument = USUARIOS+"/"+usuario.getNick()+"/"+CONEXIONES+"/"+idConexion;
                            String urlMediaCollection = "vacio";

                            Conexion conexion = new Conexion(idConexion, nickUsuarioA, nickUsuarioB,urlConexionDocument,urlMediaCollection);
                            db.document(urlConexionDocument).set(conexion);

                            // detalles del mensaje de creación.
                            String id = nick+Mensaje.TIPO_CREAR_USUARIO;
                            nick = usuario.getNick();
                            String contenido = Mensaje.TIPO_CREAR_USUARIO;
                            String hora = "00:00";
                            String tipo = Mensaje.TIPO_CREAR_USUARIO;
                            String urlMensajeEmisor = USUARIOS+"/"+usuario.getNick()+"/"+CONEXIONES+"/"+idConexion+"/"+MENSAJES+"/"+id;

                            Mensaje mensaje = new Mensaje(id,nick,contenido,hora,tipo,urlMensajeEmisor, urlMensajeEmisor);
                            db.document(urlMensajeEmisor).set(mensaje);

                            Toast.makeText(LoginActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();

                            cambiarToSalasDeChats();
                        }
                    }
                });
    }

    public void entrarToFirebase (String nick, final String pass){
        usuariosRef.document(nick).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Comprobamos que existe.
                        if(documentSnapshot.exists()){
                            if(pass.equals(documentSnapshot.getString(Usuario.KEY_PASS))){
                                usuario = documentSnapshot.toObject(Usuario.class);
                                cambiarToSalasDeChats();
                            }
                            else{
                                // Contraseña incorrecta.
                                Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                editTextPass.setText("");
                            }
                        }
                        else {
                            // No existe el usuario
                            Toast.makeText(LoginActivity.this, "No existe el usuario", Toast.LENGTH_SHORT).show();
                            editTextPass.setText("");
                            editTextNick.setText("");
                        }
                    }
                });
    }
    private void cambiarToSalasDeChats() {

        Intent intent = new Intent(LoginActivity.this, ChatsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putSerializable(USUARIO_ACTUAL,usuario);

        intent.putExtras(bundle);
        startActivity(intent);

        Toast.makeText(this, "Bienvenido: "+usuario.getNick(), Toast.LENGTH_SHORT).show();

        preferences = getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user", usuario.getNick());
        editor.putString("pass", usuario.getPass());
        editor.apply();

        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_info_login_activity_:
                    abrirInfoLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
