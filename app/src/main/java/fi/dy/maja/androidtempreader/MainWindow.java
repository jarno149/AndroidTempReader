package fi.dy.maja.androidtempreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainWindow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
    }


    // Asetetaan menun sisältö xml-tiedostossa määritellyksi
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    // Tarkistetaan mitä valikon vaihtoehtoa on painettu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.loginsettings:
                // Avataan asetusikkuna
                showLoginsettingsActivity();
                return true;

            case R.id.close:

                return true;
            default:
                return true;
        }
    }

    // Avataan ja näytetään kirjautumisasetukset
    public void showLoginsettingsActivity()
    {
        Intent settingsActivity = new Intent(this, LoginSettings.class);
        startActivity(settingsActivity);
    }

}
