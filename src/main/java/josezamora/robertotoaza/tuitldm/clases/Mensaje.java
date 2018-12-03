/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Conexion
 */

package josezamora.robertotoaza.tuitldm.clases;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@Keep
@IgnoreExtraProperties
public class Mensaje implements Serializable{

    private String id;
    private String nick;
    private String contenido;
    private String hora;
    private String tipo;
    private String urlMensajeEmisor;
    private String urlMensajeReceptor;

    public static final String KEY_ID = "id";
    public static final String KEY_NICK ="nick";
    public static final String KEY_CONTENIDO ="contenido";
    public static final String KEY_HORA ="hora";
    public static final String KEY_TIPO ="tipo";
    public static final String KEY_URL_MENSAJE_EMISOR ="urlMensajeEmisor";
    public static final String KEY_URL_MENSAJE_RECEPTOR = "urlMensajeReceptor";

    // TIPOS DE MENSAJE:
    public static String TIPO_TEXTO = "texto";
    public static String TIPO_FOTO = "foto";
    public static String TIPO_AUDIO = "audio";
    public static String TIPO_CREAR_CONEXION = "crear_conexion";
    public static String TIPO_CREAR_USUARIO = "crear_usuario";

    public Mensaje() {}

    public Mensaje(String id, String nick, String contenido, String hora, String tipo, String urlMensajeEmisor, String urlMensajeReceptor) {
        this.id = id;
        this.nick = nick;
        this.contenido = contenido;
        this.hora = hora;
        this.tipo = tipo;
        this.urlMensajeEmisor = urlMensajeEmisor;
        this.urlMensajeReceptor = urlMensajeReceptor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrlMensajeEmisor() {
        return urlMensajeEmisor;
    }

    public void setUrlMensajeEmisor(String urlMensajeEmisor) {
        this.urlMensajeEmisor = urlMensajeEmisor;
    }

    public String getUrlMensajeReceptor() {
        return urlMensajeReceptor;
    }

    public void setUrlMensajeReceptor(String urlMensajeReceptor) {
        this.urlMensajeReceptor = urlMensajeReceptor;
    }

}
