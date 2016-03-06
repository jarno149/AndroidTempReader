package fi.dy.maja.androidtempreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginSettings extends AppCompatActivity {

    private EditText username;
    private EditText passwd;
    private EditText domain;

    private static String Username;
    private static String Password;
    private static String Domain;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_settings);

        // Alustetaan property-muuttujat
        this.username = (EditText)findViewById(R.id.usernameTb);
        this.passwd = (EditText)findViewById(R.id.passwdTb);
        this.domain = (EditText)findViewById(R.id.domainTb);

        // Lisätään vanhat tiedot tekstikenttiin
        CheckSettingsIfExists();
    }

    // Luetaan jo mahdollisesti olemassaolevat tiedot
    private void CheckSettingsIfExists()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
        //SharedPreferences sharedPreferences = getPreferences(MainWindow.context);
        this.username.setText(sharedPreferences.getString("username", ""));
        this.passwd.setText(sharedPreferences.getString("passwd", ""));
        this.domain.setText(sharedPreferences.getString("domain", ""));
    }

    public void saveSettings(View view)
    {

        String username = this.username.getText().toString();
        String passwd = this.passwd.getText().toString();
        String domain = this.domain.getText().toString();


        // Tallennetaan tieto talteen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
        //SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("passwd", SHA1(passwd));

        if(!domain.startsWith("http://"))
        {
            domain = "http://" + domain;
            this.domain.setText(domain);
        }

        editor.putString("domain", domain);
        editor.commit();

        Toast.makeText(getApplicationContext(), "Tiedot tallennettu", Toast.LENGTH_LONG).show();
    }

    private String SHA1(String string)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(string.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b: bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }
}
