/*
 * TUITLDM: Practica 2 de LDM
 * Hecho por Jose Miguel Zamora Batista.
 * Clase: Conexion
 */


package josezamora.robertotoaza.tuitldm.clases;

import android.support.annotation.Keep;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Keep
@IgnoreExtraProperties
public class Conexion implements Serializable {

    private String idConexion;
    private String nickUsuarioA;
    private String nickUsuarioB;
    private String urlConexionDocument;
    private String urlMediaCollection;

    public static final String KEY_IDCONEXION ="idConexion";
    public static final String KEY_NICKUSUARIOA = "nickUsuarioA";
    public static final String KEY_NICKUSUARIOB = "nickUsuarioA";
    public static final String KEY_MENSAJESLIST = "listaMensajes";
    public static final String KEY_URLCOLLECTION = "urlConexionDocument";
    public static final String KEY_URLMEDIACOLLECTION = "urlMediaCollection";

    public Conexion(String idConexion, String nickUsuarioA, String nickUsuarioB, String urlConexionDocument, String urlMediaCollection) {

        this.idConexion = idConexion;
        this.nickUsuarioA = nickUsuarioA;
        this.nickUsuarioB = nickUsuarioB;
        this.urlConexionDocument = urlConexionDocument;
        this.urlMediaCollection = urlMediaCollection;

    }

    public Conexion() {}

    public String getIdConexion() {
        return idConexion;
    }

    public void setIdConexion(String idConexion) {
        this.idConexion = idConexion;
    }

    public String getNickUsuarioA() {
        return nickUsuarioA;
    }

    public void setNickUsuarioA(String nickUsuarioA) {
        this.nickUsuarioA = nickUsuarioA;
    }

    public String getNickUsuarioB() {
        return nickUsuarioB;
    }

    public void setNickUsuarioB(String nickUsuarioB) {
        this.nickUsuarioB = nickUsuarioB;
    }

    public String getUrlConexionDocument() {
        return urlConexionDocument;
    }

    public void setUrlConexionDocument(String urlConexionDocument) {
        this.urlConexionDocument = urlConexionDocument;
    }

    public String getUrlMediaCollection() {
        return urlMediaCollection;
    }

    public void setUrlMediaCollection(String urlMediaCollection) {
        this.urlMediaCollection = urlMediaCollection;
    }

    // --------------------------- MÉTODOS ------------------------------//
    public static String crearConexion(String a, String b){
        return a+"_"+b;
    }

}
