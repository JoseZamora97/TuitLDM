/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Adaptador de los chats.
 */


package josezamora.robertotoaza.tuitldm.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import josezamora.robertotoaza.tuitldm.R;
import josezamora.robertotoaza.tuitldm.clases.Conexion;

public class ChatsAdapter
        extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>
        implements View.OnClickListener{

    private List<Conexion> conexiones;
    private View.OnClickListener listener;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();


    ChatsAdapter(List<Conexion> conexiones){
        this.conexiones = conexiones;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_chat, viewGroup, false);
        v.setOnClickListener(this);
        return new ChatViewHolder(v);
    }

    void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i) {
        chatViewHolder.nick.setText(conexiones.get(i).getNickUsuarioB());

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.child("Usuarios").child(conexiones.get(i).getNickUsuarioB()).child("foto_perfil").child(conexiones.get(i).getNickUsuarioB()+".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        chatViewHolder.imageView.setImageBitmap(bmp);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return conexiones.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onClick(View v) {
        if(listener!=null){
            listener.onClick(v);
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{

        CardView ll;
        TextView nick;
        ImageView imageView;

        ChatViewHolder(View itemView) {
            super(itemView);
            ll =  itemView.findViewById(R.id.cardView_chats);
            nick = itemView.findViewById(R.id.textView_nickChat);
            imageView = itemView.findViewById(R.id.civ_imagenMensaje);
        }
    }
}