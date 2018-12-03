/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Activity de los mensajes.
 */


package josezamora.robertotoaza.tuitldm.chat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import josezamora.robertotoaza.tuitldm.R;
import josezamora.robertotoaza.tuitldm.clases.Conexion;
import josezamora.robertotoaza.tuitldm.clases.Mensaje;
import josezamora.robertotoaza.tuitldm.clases.Usuario;


public class MensajesActivity extends AppCompatActivity {

    static final String USUARIOS = "Usuarios";
    static final String CONEXIONES = "Conexiones";
    static final String MENSAJES = "Mensajes";
    static final String USUARIO_ACTUAL ="u_actual";
    private static final String USUARIO_CONEXION ="u_conexion";
    private static final int PICK_IMAGE_REQUEST = 55;

    private static Boolean ES_MIC = true;
    private static Boolean ES_ENVIAR = false;
    private static Boolean GRABANDO = false;

    Usuario usuarioA;
    Usuario usuarioB;
    Conexion conexion;

    List<Mensaje> mensajes;

    // Elementos de Firebase Database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usuariosRef = db.collection(USUARIOS);
    // Elementos de Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    // Elementos de la interfaz.
    TextView txtBarraNombre_chat;
    EditText editText;
    ImageButton buttonEnviarOMic;
    RecyclerView rvListaMensjes;
    LinearLayoutManager llm;
    MensajesAdapter mensajesAdapter;
    Bundle bundle;
    CircleImageView imgPerfl;
    CircleImageView img_perfil;
    private String mFileName = null;
    private MediaRecorder mRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_foreground);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(this,R.style.estiloFuenteActionBar);
        setSupportActionBar(toolbar);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"recorded_audio.3gp";

        ImageButton buttonConfigConexion = findViewById(R.id.button_configNickB);
        ImageButton buttonAtras = findViewById(R.id.imgAtras);
        ImageButton buttonGaleria = findViewById(R.id.button_camara);
        imgPerfl = findViewById(R.id.iv_imagenMensaje);

        buttonEnviarOMic = findViewById(R.id.btn_enviar_y_mic);
        buttonEnviarOMic.setImageDrawable(getDrawable(R.drawable.ic_mic_blanco));

        txtBarraNombre_chat = findViewById(R.id.nombre);
        editText = findViewById(R.id.txt_mensaje);
        rvListaMensjes = findViewById(R.id.listaMensajes);

        llm = new LinearLayoutManager(this);
        rvListaMensjes.setLayoutManager(llm);

        mensajes = new ArrayList<>();

        mensajesAdapter = new MensajesAdapter(mensajes);
        rvListaMensjes.setAdapter(mensajesAdapter);

        obtenerUsuariosYConexion();

        txtBarraNombre_chat.setText(usuarioB.getNick());

        buttonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrocederAChat();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editText.getText().toString().equals("")) {
                    buttonEnviarOMic.setImageDrawable(getDrawable(R.drawable.ic_mic_blanco));
                    MensajesActivity.ES_MIC = true;
                    MensajesActivity.ES_ENVIAR = false;
                }else {
                    buttonEnviarOMic.setImageDrawable(getDrawable(R.drawable.ic_enviar_blanco));
                    MensajesActivity.ES_MIC = false;
                    MensajesActivity.ES_ENVIAR = true;
                }

            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonEnviarOMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MensajesActivity.ES_MIC && MensajesActivity.GRABANDO) {
                    buttonEnviarOMic.setImageDrawable(getDrawable(R.drawable.ic_mic_blanco));
                    MensajesActivity.GRABANDO = false;
                    pararGrabacion();
                }
                else if (MensajesActivity.ES_MIC && !MensajesActivity.GRABANDO) {
                    buttonEnviarOMic.setImageDrawable(getDrawable(R.drawable.ic_mic_morado));
                    MensajesActivity.GRABANDO = true;
                    iniGrabacion();
                }
                else if(MensajesActivity.ES_ENVIAR)
                    ejecutarEnvioMensajes();
                else
                    Toast.makeText(MensajesActivity.this, "Error en la asignación", Toast.LENGTH_SHORT).show();
            }
        });

        buttonConfigConexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirConfiguradorDeConexion();
            }
        });

        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ejecutarGaleria();
            }
        });

        if(!usuarioB.getUrlUsuarioImgPerfil().equals(""))
            cargarFotoOnCreate();

        db.document(conexion.getUrlConexionDocument()).collection(MENSAJES)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        assert snapshots != null;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Mensaje m = dc.getDocument().toObject(Mensaje.class);
                                    if(!m.getTipo().equals(Mensaje.TIPO_CREAR_CONEXION))
                                            mensajes.add(m);

                                    Collections.sort(mensajes, new Comparator<Mensaje>() {
                                        @Override
                                        public int compare(Mensaje o1, Mensaje o2) {

                                            Integer m1 = Integer.parseInt(o1.getId());
                                            Integer m2 = Integer.parseInt(o2.getId());

                                            return m1.compareTo(m2);
                                        }
                                    });

                                    mensajesAdapter = new MensajesAdapter(mensajes);
                                    rvListaMensjes.setAdapter(mensajesAdapter);
                                    mensajesAdapter.notifyDataSetChanged();
                                    rvListaMensjes.scrollToPosition(mensajes.size()-1);
                                    break;
                            }
                        }
                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
        mensajes.clear();
        cargarMensajesFromFirebase();
    }

    @Override
    protected  void onRestart(){
        super.onRestart();
        mensajes.clear();
        mensajesAdapter.notifyDataSetChanged();
    }

    /*
     * Funcion que carga los mensajes.
     */
    private void cargarMensajesFromFirebase() {

        usuariosRef.document(usuarioA.getNick())
                .collection(CONEXIONES).document(conexion.getIdConexion())
                .collection(MENSAJES).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for(QueryDocumentSnapshot documentSnapshot : snapshots){
                            Mensaje m = documentSnapshot.toObject(Mensaje.class);
                            if(!m.getTipo().equals(Mensaje.TIPO_CREAR_CONEXION)){
                                boolean encontrado = false;

                                for(Mensaje mAux : mensajes){
                                    if(m.getId().equals(mAux.getId())){
                                        encontrado = true;
                                        break;
                                    }
                                }

                                if(!encontrado){
                                    mensajes.add(m);

                                    Collections.sort(mensajes, new Comparator<Mensaje>() {
                                        @Override
                                        public int compare(Mensaje o1, Mensaje o2) {

                                            Integer m1 = Integer.parseInt(o1.getId());
                                            Integer m2 = Integer.parseInt(o2.getId());

                                            return m1.compareTo(m2);
                                        }
                                    });
                                }
                            }
                        }

                        mensajesAdapter = new MensajesAdapter(mensajes);
                        rvListaMensjes.setAdapter(mensajesAdapter);
                        mensajesAdapter.notifyDataSetChanged();
                        rvListaMensjes.scrollToPosition(mensajes.size()-1);
                    }
                });
    }


    //------------------------------ CREAR EL CONTENIDO DEL MENSAJE DE TEXTO -------------------------------------------------------
    private void ejecutarEnvioMensajes() {
        enviarMensaje(editText.getText().toString(), obtenerHora(), Mensaje.TIPO_TEXTO);
    }

    //------------------------------ CREAR EL CONTENIDO DEL MENSAJE DE FOTO -------------------------------------------------------
    private void ejecutarGaleria() {
        abrirGaleria();
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
            Uri uri = data.getData();
            abrirEnviarImagen(uri);
        }
    }

    private void abrirEnviarImagen(Uri uri) {

        if (uri != null) {

            StorageReference refA = storageRef.child(USUARIOS)
                    .child(usuarioA.getNick())
                    .child(CONEXIONES)
                    .child(conexion.getIdConexion())
                    .child(MENSAJES)
                    .child(Integer.toString(mensajes.size()+1)+".jpg");

            refA.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String contenido = USUARIOS+"/"+usuarioA.getNick()+"/"
                                        +CONEXIONES+"/"+conexion.getIdConexion() +"/"
                                        +MENSAJES+"/"+Integer.toString(mensajes.size()+1)+".jpg";

                    enviarMensaje(contenido, obtenerHora(), Mensaje.TIPO_FOTO);
                }
            });
        }
    }

    //------------------------------ CREAR EL CONTENIDO DEL MENSAJE DE AUDIO -------------------------------------------------------

    private void iniGrabacion() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();
        Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show();
    }

    private void pararGrabacion() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        Toast.makeText(this, "Enviando audio...", Toast.LENGTH_SHORT).show();
        subirAudio();
    }

    private void subirAudio() {
            Uri uri =Uri.fromFile(new File(mFileName));

            StorageReference refA = storageRef.child(USUARIOS)
                    .child(usuarioA.getNick())
                    .child(CONEXIONES)
                    .child(conexion.getIdConexion())
                    .child(MENSAJES)
                    .child(Integer.toString(mensajes.size()+1)+".3gp");

            refA.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String contenido = USUARIOS+"/"+usuarioA.getNick()+"/"
                            +CONEXIONES+"/"+conexion.getIdConexion() +"/"
                            +MENSAJES+"/"+Integer.toString(mensajes.size()+1)+".3gp";

                    enviarMensaje(contenido, obtenerHora(), Mensaje.TIPO_AUDIO);
                }
            });
    }

    //------------------------------------------------------------------------------------------------------------------------------
    /*
     * Funcion que manda el mensaje.
     */
    private void enviarMensaje(String contenido, String hora, String tipo) {

        String id = Integer.toString(mensajes.size()+1);

        String urlMensajeEmisor = USUARIOS + "/" + usuarioA.getNick() + "/"
                + CONEXIONES + "/"+ conexion.getIdConexion() + "/"
                + MENSAJES + "/" + Integer.toString(mensajes.size()+1);

        String urlMensajeReceptor = USUARIOS + "/" + usuarioB.getNick() + "/"
                + CONEXIONES + "/" + Conexion.crearConexion(usuarioB.getNick(), usuarioA.getNick()) + "/"
                + MENSAJES + "/"+ Integer.toString(mensajes.size()+1);

        Mensaje mIda = new Mensaje(id, usuarioA.getNick(), contenido,
                hora, tipo, urlMensajeEmisor, urlMensajeReceptor);
        Mensaje mVuelta = new Mensaje(id, usuarioA.getNick(), contenido,
                hora, tipo, urlMensajeEmisor, urlMensajeReceptor);

        db.document(urlMensajeEmisor).set(mIda);
        db.document(urlMensajeReceptor).set(mVuelta);

        editText.setText("");
        mensajesAdapter.notifyDataSetChanged();
    }

    /**
     * Funcion que obtiene los usuarios y la conexion.
     */
    private void obtenerUsuariosYConexion() {
        bundle = getIntent().getExtras();

        assert bundle != null;
        usuarioA = (Usuario) bundle.getSerializable(USUARIO_ACTUAL);
        usuarioB = (Usuario) bundle.getSerializable(USUARIO_CONEXION);
        conexion = (Conexion) bundle.getSerializable(CONEXIONES);
    }

    /**
     * Funcion que abre la info .
     */
    private void abrirInfoSalaChat() {

        LayoutInflater li = LayoutInflater.from(MensajesActivity.this);
        AlertDialog.Builder builderInfo = new AlertDialog.Builder(MensajesActivity.this);

        @SuppressLint("InflateParams")
        View viewInfoLogin = li.inflate(R.layout.card_info_mensajes, null);

        builderInfo.setView(viewInfoLogin);

        AlertDialog alertDialog = builderInfo.create();
        alertDialog.show();

    }

    /**
     * Funcion que retrocede a ChatsActivity
     */
    private void retrocederAChat() {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(getApplicationContext(), ChatsActivity.class);
        bundle.putSerializable(USUARIO_ACTUAL, usuarioA);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    /**
     * Funcion que abre el configurador de conexión
     */
    private void abrirConfiguradorDeConexion() {

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View viewConfigConex = li.inflate(R.layout.card_configurador_conexion, null);

        TextView nick = viewConfigConex.findViewById(R.id.nick_configuradorConex);
        TextView nombre = viewConfigConex.findViewById(R.id.nombre_configuradorConex);
        TextView apellido = viewConfigConex.findViewById(R.id.apellido_configuradorConex);

        Button btn_cerrar = viewConfigConex.findViewById(R.id.btn_cerrarConexion);

        img_perfil = viewConfigConex.findViewById(R.id.img_fotoPerfConfiguradorConex);

        nick.setText(usuarioB.getNick());
        nombre.setText(usuarioB.getRealName());
        apellido.setText(usuarioB.getRealLastName());
        if(!usuarioB.getUrlUsuarioImgPerfil().equals(""))
            cargarLaFoto();

        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuariosRef.document(usuarioA.getNick())
                        .collection(CONEXIONES).document(conexion.getIdConexion())
                        .delete();
                usuariosRef.document(usuarioB.getNick())
                        .collection(CONEXIONES)
                        .document(Conexion.crearConexion(conexion.getNickUsuarioB(),conexion.getNickUsuarioA()))
                        .delete();

                retrocederAChat();
            }
        });

        android.app.AlertDialog.Builder builderConfig = new android.app.AlertDialog.Builder(this);

        builderConfig.setView(viewConfigConex)
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alertDialog = builderConfig.create();
        alertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sala_chat_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_info_salachat_activity_:
                abrirInfoSalaChat();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Funcion que devuelve la hora en el formato adecuado.
     */
    private String obtenerHora() {
        Calendar calendar = Calendar.getInstance();

        Integer hora_ = calendar.get(Calendar.HOUR_OF_DAY);
        Integer minutos_ = calendar.get(Calendar.MINUTE);
        String hora;

        if(minutos_.toString().length()==1)
            hora = hora_.toString() + ":0" + minutos_.toString();
        else
            hora = hora_.toString() + ":" + minutos_.toString();

        return hora;
    }

    private void cargarLaFoto() {

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.child(usuarioB.getUrlUsuarioImgPerfil()).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        img_perfil.setImageBitmap(bmp);
                        imgPerfl.setImageBitmap(bmp);
                    }
                });
    }

    private void cargarFotoOnCreate() {
        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.child(usuarioB.getUrlUsuarioImgPerfil()).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        imgPerfl.setImageBitmap(bmp);
                    }
                });
    }
}
