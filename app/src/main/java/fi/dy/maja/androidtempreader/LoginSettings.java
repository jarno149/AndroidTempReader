package fi.dy.maja.androidtempreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginSettings extends AppCompatActivity {

    private EditText username;
    private EditText passwd;
    private EditText domain;

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
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("passwd", passwd);
        editor.putString("domain", domain);
        editor.commit();

        Toast toast = Toast.makeText(getApplicationContext(), "Tiedot tallennettu", Toast.LENGTH_LONG);
    }

}
