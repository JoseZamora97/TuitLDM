/*
 * TuitLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Adaptador para los mensajes.
 */

package josezamora.robertotoaza.tuitldm.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import josezamora.robertotoaza.tuitldm.R;
import josezamora.robertotoaza.tuitldm.clases.Mensaje;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MensajesAdapter extends RecyclerView.Adapter<MensajesAdapter.MensajesViewHolder> {

    private List<Mensaje> mensajes;
    private MediaPlayer mp;
    private int y =0;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    MensajesAdapter(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    @NonNull
    @Override
    public MensajesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_mensaje, viewGroup, false);
        Collections.sort(mensajes, new Comparator<Mensaje>() {
                @Override
                public int compare(Mensaje o1, Mensaje o2) {

                    Integer m1 = Integer.parseInt(o1.getId());
                    Integer m2 = Integer.parseInt(o2.getId());

                    return m1.compareTo(m2);
                }
        });
        return new MensajesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MensajesViewHolder mensajesViewHolder, int i) {

        if(mensajes.get(i).getTipo().equals(Mensaje.TIPO_TEXTO)){
            mensajesViewHolder.nick.setText(mensajes.get(i).getNick());
            mensajesViewHolder.hora.setText(mensajes.get(i).getHora());
            mensajesViewHolder.ll.removeAllViewsInLayout();
            mensajesViewHolder.mensaje.setText(mensajes.get(i).getContenido());
            mensajesViewHolder.ll.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mensajesViewHolder.ll.addView(mensajesViewHolder.mensaje);
        }
        else if(mensajes.get(i).getTipo().equals(Mensaje.TIPO_FOTO)){
            mensajesViewHolder.nick.setText(mensajes.get(i).getNick());
            mensajesViewHolder.hora.setText(mensajes.get(i).getHora());
            mensajesViewHolder.ll.removeAllViewsInLayout();
            mensajesViewHolder.ll.setBackgroundColor(Color.parseColor("#d1c4e9"));

            final long TEN_MEGABYTE = 10 * 1024 * 1024;
            StorageReference stoRef  = storageRef.child(mensajes.get(i).getContenido());
            stoRef.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    mensajesViewHolder.imgView.setImageBitmap(bmp);
                    mensajesViewHolder.imgView.getLayoutParams().height=MATCH_PARENT;
                    mensajesViewHolder.imgView.getLayoutParams().width=MATCH_PARENT;
                }
            });

            mensajesViewHolder.ll.addView(mensajesViewHolder.imgView);
        }
        else if(mensajes.get(i).getTipo().equals(Mensaje.TIPO_AUDIO)) {

            mensajesViewHolder.nick.setText(mensajes.get(i).getNick());
            mensajesViewHolder.hora.setText(mensajes.get(i).getHora());
            mensajesViewHolder.ll.removeAllViewsInLayout();
            mensajesViewHolder.mensaje.setText(String.format("%s %s", mensajes.get(i).getNick(),
                    mensajesViewHolder.itemView.getContext().getResources().getString(R.string.enviado_audio)));
            mensajesViewHolder.mensaje.setGravity(Gravity.CENTER);
            mensajesViewHolder.ll.setBackgroundColor(Color.parseColor("#d1c4e9"));
            mensajesViewHolder.ll.addView(mensajesViewHolder.mensaje);


            final long TEN_MEGABYTE = 10 * 1024 * 1024;
            StorageReference stoRef  = storageRef.child(mensajes.get(i).getContenido());
            stoRef.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {

                @Override
                public void onSuccess(byte[] bytes) {
                    mp = byteArrayToMediaPlayer(bytes, mensajesViewHolder.itemView.getContext());
                    try {
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mensajesViewHolder.play = true;
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.seekTo(0);
                            y = 0;
                            mensajesViewHolder.imgButton
                                    .setImageDrawable(mensajesViewHolder.itemView
                                            .getResources().getDrawable(R.drawable.ic_play_morado, null));
                            mensajesViewHolder.play=true;
                        }
                    });


                    mensajesViewHolder.imgButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            if(mensajesViewHolder.play){

                                mensajesViewHolder.imgButton
                                        .setImageDrawable(mensajesViewHolder.itemView
                                                .getResources().getDrawable(R.drawable.ic_pausa_morado, null));
                                mensajesViewHolder.play=false;

                                mp.seekTo(y);
                                mp.start();

                            }else{

                                y=mp.getCurrentPosition();
                                mp.pause();

                                mensajesViewHolder.imgButton
                                        .setImageDrawable(mensajesViewHolder.itemView
                                                .getResources().getDrawable(R.drawable.ic_play_morado, null));
                                mensajesViewHolder.play=true;

                            }
                        }
                    });
                }
            });

            mensajesViewHolder.ll.addView(mensajesViewHolder.imgButton);

        }else
            mensajesViewHolder.mensaje.setText( mensajesViewHolder.itemView.getContext().getResources().getString(R.string.e));
    }

    @Override
    public int getItemCount() {
        return mensajes.size();}

    private static MediaPlayer byteArrayToMediaPlayer(byte[] cancion, Context context){

        MediaPlayer mp = new MediaPlayer();

        try {
            File temp3GP = File.createTempFile("audio","3gp",context.getCacheDir());
            temp3GP.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(temp3GP);
            fos.write(cancion);
            fos.close();

            FileInputStream fis = new FileInputStream(temp3GP);
            mp.setDataSource(fis.getFD());
        }catch (IOException ignored){}

        return mp;
    }

    class MensajesViewHolder extends RecyclerView.ViewHolder {

        TextView nick;
        TextView mensaje;
        TextView hora;
        CardView cv;
        LinearLayout ll;
        ImageView imgView;
        ImageButton imgButton;
        Boolean play;
        Context context;

        MensajesViewHolder(@NonNull final View itemView) {
            super(itemView);

            context = itemView.getContext();
            ll = itemView.findViewById(R.id.ll_mensaje);
            nick = itemView.findViewById(R.id.tv_nickMensaje);
            mensaje = itemView.findViewById(R.id.tv_mensajeMensaje);
            hora = itemView.findViewById(R.id.tv_horaMensaje);
            cv = itemView.findViewById(R.id.cardView_mensajes);

            //------------------------------------------------------ IMAGEN ----------------------------------------------------------------

            imgView = new ImageView(itemView.getContext());

            int anchoImg = 350;
            int altoImg = 350;

            imgView.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_cam_morado));
            LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(anchoImg, altoImg);
            imgView.setLayoutParams(paramsImg);

            imgView.setPadding(5,10 , 10, 5);
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imgView.setAdjustViewBounds(true);


            //------------------------------------------------------ AUDIO -----------------------------------------------------------------

            imgButton = new ImageButton(itemView.getContext());

            int anchoAud = 150;
            int altoAud = 150;

            LinearLayout.LayoutParams paramsAud = new LinearLayout.LayoutParams(anchoAud, altoAud);
            imgButton.setLayoutParams(paramsAud);
            imgButton.setPadding(5, 5, 5, 5);
            imgButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            imgButton.setAdjustViewBounds(true);
            imgButton.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_play_morado, null));
            imgButton.setBackgroundColor(itemView.getResources().getColor(R.color.transparente));



            //------------------------------------------------------------------------------------------------------------------------------
        }
    }
}
