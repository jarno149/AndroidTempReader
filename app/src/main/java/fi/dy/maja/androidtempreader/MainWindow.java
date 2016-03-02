package fi.dy.maja.androidtempreader;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainWindow extends AppCompatActivity
{
    public ListView dateList;
    public static SharedPreferences preferences;
    public static boolean DataIsSet = false;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        // Alustetaan luokan property context
        context = this;

        //setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, asd));

        // Alustetaan luokan listview olio.
        dateList = (ListView)findViewById(R.id.list);

        // Haetaan mittauspäivien suurpiirteiset tiedot
        try
        {
            String data = new GetMeasurementDates().execute("function=listdates").get();
            SetDataToList(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Asetetaan mittauspäivien tiedot pääikkunan listaan
    public void SetDataToList(String data)
    {
        // Tarkistetaan että listassa on jotain
        if(data != null && data != "")
        {
            String[] dateMeasurements;
            try
            {
                // Parsitaan JSON-dataa tekstistä
                JSONArray jsonArray = new JSONArray(data);
                dateMeasurements = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jobj = jsonArray.getJSONObject(i);
                    double out = jobj.getDouble("outavg");
                    double in = jobj.getDouble("inavg");
                    dateMeasurements[i] = jobj.getString("date") + "#Out: " + String.format("%.2f", out) + "\u2103" + "#In: " + String.format("%.2f", in) + "\u2103";
                }
                this.dateList.setAdapter(new testi(this, dateMeasurements));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
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
                    finish();
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
