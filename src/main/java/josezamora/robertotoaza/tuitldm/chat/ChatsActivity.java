/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Activity de los Chats.
 */
package josezamora.robertotoaza.tuitldm.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import josezamora.robertotoaza.tuitldm.R;
import josezamora.robertotoaza.tuitldm.clases.Conexion;
import josezamora.robertotoaza.tuitldm.clases.Mensaje;
import josezamora.robertotoaza.tuitldm.clases.Usuario;
import josezamora.robertotoaza.tuitldm.login.LoginActivity;

public class ChatsActivity extends AppCompatActivity {

    static final String USUARIOS = "Usuarios";
    static final String CONEXIONES ="Conexiones";
    private static final String MENSAJES = "Mensajes";
    private static final String USUARIO_ACTUAL ="u_actual";
    private static final String USUARIO_CONEXION ="u_conexion";
    private static final String PREFERENCIAS = "Preferencias";

    private static final String FOTO_PERFIL = "foto_perfil";

    Usuario usuario;
    Usuario usuario_conexion;
    List<Conexion> conexiones;
    SharedPreferences preferences;

    // Elementos de Firebase Database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usuariosRef = db.collection(USUARIOS);    // referencia a los usuarios.
    CollectionReference conexionesRef;

    // Elementos de Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private final int PICK_IMAGE_REQUEST = 71 ;

    // Elementos de la interfaz.
    TextView txtBarraNombre_chat;
    RecyclerView rvListaConexiones;
    ChatsAdapter adapterChats;
    ImageButton buttonConfigurarUsuario;
    LinearLayoutManager llm;
    Bundle bundle;
    CircleImageView imgPerfil;
    CircleImageView img_perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_foreground);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(this,R.style.estiloFuenteActionBar);
        setSupportActionBar(toolbar);

        buttonConfigurarUsuario = findViewById(R.id.imgBuscar);
        txtBarraNombre_chat = findViewById(R.id.textView_chatIDChats);
        imgPerfil = findViewById(R.id.imagen_perfil_nickA);

        llm = new LinearLayoutManager(this);

        rvListaConexiones = findViewById(R.id.rv_listaMensajes);
        rvListaConexiones.setHasFixedSize(true);
        rvListaConexiones.setLayoutManager(llm);

        obtenerUsuario();

        conexiones = new ArrayList<>();
        conexionesRef = usuariosRef.document(usuario.getNick()).collection(CONEXIONES);
        txtBarraNombre_chat.setText(usuario.getNick());

        adapterChats = new ChatsAdapter(conexiones);
        rvListaConexiones.setAdapter(adapterChats);

        adapterChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = v.findViewById(R.id.textView_nickChat);

                usuariosRef.document(textView.getText().toString()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                // Comprobamos que existe.
                                if(documentSnapshot.exists()){
                                    usuario_conexion = documentSnapshot.toObject(Usuario.class);
                                    assert usuario_conexion != null;
                                    cambiarASalaDeChat(usuario_conexion.getNick());
                                }
                            }
                        });
            }
        });

        buttonConfigurarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { 
                abrirConfigurarUsuario();
            }
        });

        if(!usuario.getUrlUsuarioImgPerfil().equals(""))
            cargarFotoOnCreate();

        usuariosRef.document(usuario.getNick()).collection(CONEXIONES)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        assert snapshots != null;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Conexion c = dc.getDocument().toObject(Conexion.class);
                                    if(!c.getNickUsuarioA().equals(c.getNickUsuarioB()))
                                        conexiones.add(0, c);
                                    adapterChats.notifyDataSetChanged();
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        conexiones.clear();
        obtenerConexionesFromFirebase();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        conexiones.clear();
        adapterChats.notifyDataSetChanged();
    }

    /*
     * Función que muestra la info de la activity.
     */
    private void abrirInfoChats() {

        LayoutInflater li = LayoutInflater.from(ChatsActivity.this);
        AlertDialog.Builder builderChats = new AlertDialog.Builder(ChatsActivity.this);

        @SuppressLint("InflateParams")
        View viewInfoChats = li.inflate(R.layout.card_info_chats, null);

        builderChats.setView(viewInfoChats);

        AlertDialog alertDialog = builderChats.create();
        alertDialog.show();

    }

    /*
     * Funcion que permite configurar los parámetros del usuario.
     */
    private void abrirConfigurarUsuario() {

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View viewConfigUsuario = li.inflate(R.layout.card_configurador_usuario, null);

        TextView nick = viewConfigUsuario.findViewById(R.id.nick_configurador);
        final EditText pass = viewConfigUsuario.findViewById(R.id.pass_configurador);
        final EditText nombre = viewConfigUsuario.findViewById(R.id.nombre_configurador);
        final EditText apellido = viewConfigUsuario.findViewById(R.id.apellido_configurador);

        ImageButton btn_editarPass = viewConfigUsuario.findViewById(R.id.btn_editarPass);
        ImageButton btn_editarNombre = viewConfigUsuario.findViewById(R.id.btn_editarNombre);
        ImageButton btn_editarApellido = viewConfigUsuario.findViewById(R.id.btn_editarApellido);

        Button btn_borrar = viewConfigUsuario.findViewById(R.id.btn_borrarCuenta);
        Button btn_cerrar = viewConfigUsuario.findViewById(R.id.btn_cerrarCuenta);

        img_perfil = viewConfigUsuario.findViewById(R.id.img_fotoPerfilConfigurador);

        img_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        nick.setText(usuario.getNick());
        pass.setText(usuario.getPass());
        nombre.setText(usuario.getRealName());
        apellido.setText(usuario.getRealLastName());
        if(!usuario.getUrlUsuarioImgPerfil().equals(""))
            cargarLaFoto();

        btn_editarNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuariosRef.document(usuario.getNick()).update(Usuario.KEY_REALNAME,nombre.getText().toString());
                usuariosRef.document(usuario.getNick()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                usuario = documentSnapshot.toObject(Usuario.class);
                                Toast.makeText(ChatsActivity.this,  getString(R.string.exito_actualizar_nombre), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btn_editarApellido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuariosRef.document(usuario.getNick()).update(Usuario.KEY_REALLASTNAME,apellido.getText().toString());
                usuariosRef.document(usuario.getNick()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                usuario = documentSnapshot.toObject(Usuario.class);
                                Toast.makeText(ChatsActivity.this,  getString(R.string.exitoActualizarApellido), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        btn_editarPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 usuariosRef.document(usuario.getNick()).update(Usuario.KEY_PASS, pass.getText().toString());
                 usuariosRef.document(usuario.getNick()).get()
                         .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                             @Override
                             public void onSuccess(DocumentSnapshot documentSnapshot) {
                                 usuario = documentSnapshot.toObject(Usuario.class);
                                 Toast.makeText(ChatsActivity.this,  getString(R.string.exito_actuoalizar_contrase_a), Toast.LENGTH_SHORT).show();
                             }
                         });
            }
        });

        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

        btn_borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarUsuarioActual();

            }
        });

        //-------------------------------------------------------------------------
        AlertDialog.Builder builderConfig = new AlertDialog.Builder(this);

        builderConfig.setView(viewConfigUsuario)
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                           dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builderConfig.create();
        alertDialog.show();

    }

    private void borrarUsuarioActual() {

        usuariosRef.document(usuario.getNick())
                .collection(CONEXIONES).document(Conexion.crearConexion(usuario.getNick(),usuario.getNick())).delete();

        for(Conexion c : conexiones){
            usuariosRef.document(usuario.getNick())
                    .collection(CONEXIONES).document(c.getIdConexion())
                    .collection(MENSAJES).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : snapshots){
                                documentSnapshot.getReference().delete();
                            }
                        }
                    });

            usuariosRef.document(c.getNickUsuarioB())
                    .collection(CONEXIONES).document(Conexion.crearConexion(c.getNickUsuarioB(),c.getNickUsuarioA()))
                    .collection(MENSAJES).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : snapshots){
                                documentSnapshot.getReference().delete();
                            }
                        }
                    });

            usuariosRef.document(c.getNickUsuarioB())
                    .collection(CONEXIONES).document(Conexion.crearConexion(c.getNickUsuarioB(),c.getNickUsuarioA())).delete();
            usuariosRef.document(usuario.getNick())
                    .collection(CONEXIONES).document(c.getIdConexion()).delete();
        }

        usuariosRef.document(usuario.getNick()).delete();

        cerrarSesion();
    }

    private void cargarLaFoto() {

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.child(usuario.getUrlUsuarioImgPerfil()).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    img_perfil.setImageBitmap(bmp);
                    imgPerfil.setImageBitmap(bmp);
                }
            });
    }

    private void cargarFotoOnCreate() {

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.child(usuario.getUrlUsuarioImgPerfil()).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        imgPerfil.setImageBitmap(bmp);
                    }
                });
    }

    private void abrirGaleria() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            CropImage.activity(filePath).setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                assert result != null;
                Uri resultUri = result.getUri();
                imgPerfil.setImageURI(resultUri);
                img_perfil.setImageURI(resultUri);

                abrirEnviarImagen(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                result.getError();
            }
        }
    }

    /*
     * Funcion que envia la imagen.
     */
    private void abrirEnviarImagen(Uri uri) {
        if (uri != null) {

            StorageReference ref = storageRef.child(USUARIOS)
                    .child(usuario.getNick())
                    .child(FOTO_PERFIL)
                    .child(usuario.getNick()+".jpg");

            usuariosRef.document(usuario.getNick()).update(Usuario.KEY_URLUSUARIOIMGPERFIL,ref.getPath());
            usuariosRef.document(usuario.getNick()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            usuario = documentSnapshot.toObject(Usuario.class);
                        }
                    });

            usuario.setUrlUsuarioImgPerfil(ref.getPath());

            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ChatsActivity.this, "Subida con éxito.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /*
     * Funcion que cierra la sesion y reestable el Shared Preferences.
     */
    private void cerrarSesion() {
        preferences = getSharedPreferences(PREFERENCIAS,Context.MODE_PRIVATE);
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("user", "0");
       editor.putString("pass", "0");
       editor.apply();

       Intent i = new Intent(this, LoginActivity.class);
       startActivity(i);
       this.finish();
    }

    /*
     * Función que avanza a la activity mensajes.
     */
    private void cambiarASalaDeChat(String nick) {
        Intent intent = new Intent(ChatsActivity.this, MensajesActivity.class);

        bundle = new Bundle();

        for(Conexion conexion : conexiones){
            if(conexion.getNickUsuarioB().equals(nick)){
                bundle.putSerializable(CONEXIONES, conexion);
                break;
            }
        }

        bundle.putSerializable(USUARIO_ACTUAL, usuario);
        bundle.putSerializable(USUARIO_CONEXION, usuario_conexion);

        intent.putExtras(bundle);
        startActivity(intent);

        this.finish();
    }

    /*
     * Función que conecta un usuario con el usuario actual.
     */
    private void conectarCon(Usuario nuevoUsuario) {

        Conexion cIda, cVuelta;

        Calendar calendar = Calendar.getInstance();
        Integer hora_ = calendar.get(Calendar.HOUR_OF_DAY);
        Integer minutos_ = calendar.get(Calendar.MINUTE);
        String hora;

        if(minutos_.toString().length()==1)
            hora = hora_.toString() + ":0" + minutos_.toString();
        else
            hora = hora_.toString() + ":" + minutos_.toString();

        String urlConexionDocument;

        urlConexionDocument = USUARIOS + "/" + usuario.getNick() + "/"
                + CONEXIONES + "/" + Conexion.crearConexion(usuario.getNick(), nuevoUsuario.getNick());

        cIda = new Conexion(Conexion.crearConexion(usuario.getNick(), nuevoUsuario.getNick()), usuario.getNick(), nuevoUsuario.getNick(), urlConexionDocument, "vacio");

        urlConexionDocument = USUARIOS + "/" + nuevoUsuario.getNick() + "/"
                + CONEXIONES + "/" + Conexion.crearConexion(nuevoUsuario.getNick(), usuario.getNick());

        cVuelta = new Conexion(Conexion.crearConexion(nuevoUsuario.getNick(), usuario.getNick()), nuevoUsuario.getNick(), usuario.getNick(), urlConexionDocument, "vacio");

        String urlMensajeEmisor = USUARIOS + "/" + usuario.getNick() + "/"
                + CONEXIONES + "/"+ cIda.getIdConexion() + "/"
                + MENSAJES + "/" +"0";

        String urlMensajeReceptor = USUARIOS + "/" + nuevoUsuario.getNick() + "/"
                + CONEXIONES +"/"+ cVuelta.getIdConexion() + "/"
                + MENSAJES +"/"+ "0";

        Mensaje mIda = new Mensaje("0", usuario.getNick(), getString(R.string.startConexion) + nuevoUsuario.getNick(),
                hora, Mensaje.TIPO_CREAR_CONEXION, urlMensajeEmisor, urlMensajeReceptor);
        Mensaje mVuelta = new Mensaje("0", nuevoUsuario.getNick(), getString(R.string.startConexion) + usuario.getNick(),
                hora, Mensaje.TIPO_CREAR_CONEXION, urlMensajeEmisor, urlMensajeReceptor);

        db.document(cIda.getUrlConexionDocument()).set(cIda);
        db.document(cVuelta.getUrlConexionDocument()).set(cVuelta);
        db.document(urlMensajeEmisor).set(mIda);
        db.document(urlMensajeReceptor).set(mVuelta);

        conexiones.clear();

        Intent i = new Intent(this, ChatsActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(USUARIO_ACTUAL,usuario);
        i.putExtras(b);
        startActivity(i);
        this.finish();
    }

    /*
     * Funcion que llena la lista de conexiones desde Firebase.
     */
    private void obtenerConexionesFromFirebase() {

        usuariosRef.document(usuario.getNick()).collection(CONEXIONES).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for(QueryDocumentSnapshot documentSnapshot : snapshots){
                            Conexion c = documentSnapshot.toObject(Conexion.class);
                            if(!c.getNickUsuarioA().equals(c.getNickUsuarioB())){

                                boolean encontrado = false;

                                for(Conexion cAux: conexiones){
                                    if(c.getIdConexion().equals(cAux.getIdConexion())){
                                       encontrado = true;
                                       break;
                                    }
                                }

                                if(!encontrado){
                                    conexiones.add(c);
                                }
                            }
                        }
                        adapterChats.notifyDataSetChanged();
                    }
                });
    }

    /*
     * Funcion que recoge el bundle de la activity anterior.
     */
    private void obtenerUsuario() {
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        usuario = (Usuario) bundle.getSerializable(USUARIO_ACTUAL);
    }

    /*
     * Función que comprueba si un usuario esta conectado con el usuario actual.
     */
    private boolean estanConectados(final String nickUsuarioB) {
        for(Conexion c : conexiones){
            if(c.getNickUsuarioB().equals(nickUsuarioB))
                return true;
        }
        return false;
    }

    /*
     * Función que asigna el menú creado a la Action Bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chats_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Función que comprueba cual boton del menu se tocó.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_info_chats_activity_:
                abrirInfoChats();
                return true;
            case R.id.accion_buscar_chats_activity_:
                abrirBuscarChats();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Funcion que abre el AlertDialog de buscar usuario.
     */
    private void abrirBuscarChats() {
        alertDialogCrearConexion();
    }

    /*
     * Dialogo de creación de un nuevo usuario.
     */
    private void alertDialogCrearConexion() {
        LayoutInflater li = LayoutInflater.from(this);

        @SuppressLint("InflateParams") final View viewCrearConexion = li.inflate(R.layout.card_crear_conexion, null);
        final EditText nickABuscar = viewCrearConexion.findViewById(R.id.editText_nickCrearConexion);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setView(viewCrearConexion)
                .setCancelable(false)
                .setPositiveButton(this.getResources().getText(R.string.añadir), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if(nickABuscar.getText().toString().equals("")) // Si el nick esta vacio.
                            Toast.makeText(ChatsActivity.this, "Introduce un nick", Toast.LENGTH_SHORT).show();
                        else{
                            usuariosRef
                                    .document(nickABuscar.getText().toString())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) { // SI EXISTE EL DOCUMENTO USUARIO con nick el del edit text.

                                                if (usuario.getNick().equals(nickABuscar.getText().toString())) // Compruebo si es mi nick.
                                                    Toast.makeText(ChatsActivity.this, getResources().getString(R.string.No_conexión_contigo_mismo), Toast.LENGTH_SHORT).show();

                                                else // si no es mi nick.
                                                {
                                                    if (estanConectados(nickABuscar.getText().toString())) { // Compruebo si estoy conectado con el.
                                                        Toast.makeText(ChatsActivity.this, getResources().getString(R.string.Ya_estas_conectado_ese_usuario), Toast.LENGTH_SHORT).show();
                                                    }
                                                    else { // si no estoy conectado creo la conexion.
                                                        Usuario nuevoUsuario = documentSnapshot.toObject(Usuario.class);
                                                        assert nuevoUsuario != null;
                                                        conectarCon(nuevoUsuario);
                                                        Toast.makeText(ChatsActivity.this, nickABuscar.getText().toString() + getString(R.string.as), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                dialog.cancel();
                                                Toast.makeText(ChatsActivity.this, "No se encontró ese usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton(this.getResources().getText(R.string.Cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

