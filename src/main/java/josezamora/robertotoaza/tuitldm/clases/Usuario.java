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
public class Usuario implements Serializable {

    private String nick;
    private String pass;
    private String realName;
    private String realLastName;
    private String urlUsuarioDocument;
    private String urlUsuarioImgPerfil;

    public static final String KEY_NICK = "nick";
    public static final String KEY_PASS = "pass";
    public static final String KEY_REALNAME = "realName";
    public static final String KEY_REALLASTNAME = "realLastName";
    public static final String KEY_URLUSUARIODOCUMENT = "urlUsuarioDocument";
    public static final String KEY_URLUSUARIOIMGPERFIL = "urlUsuarioImgPerfil";
    
    public Usuario(String nick, String pass, String realName, String realLastName, String urlUsuarioDocument, String urlUsuarioImgPerfil) {
        this.nick = nick;
        this.pass = pass;
        this.realName = realName;
        this.realLastName = realLastName;
        this.urlUsuarioDocument = urlUsuarioDocument;
        this.urlUsuarioImgPerfil = urlUsuarioImgPerfil;
    }

    public Usuario() {}

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealLastName() {
        return realLastName;
    }

    public void setRealLastName(String realLastName) {
        this.realLastName = realLastName;
    }

    public String getUrlUsuarioDocument() {
        return urlUsuarioDocument;
    }

    public void setUrlUsuarioDocument(String urlUsuarioDocument) {
        this.urlUsuarioDocument = urlUsuarioDocument;
    }

    public String getUrlUsuarioImgPerfil() {
        return urlUsuarioImgPerfil;
    }

    public void setUrlUsuarioImgPerfil(String urlUsuarioImgPerfil) {
        this.urlUsuarioImgPerfil = urlUsuarioImgPerfil;
    }

}
