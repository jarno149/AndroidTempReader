package fi.dy.maja.androidtempreader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainWindow extends AppCompatActivity
{
    public static ListView dateList;
    public static DateListObject[] dateListObjects;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        // Alustetaan luokan staattiset muuttujat
        context = this;

        // Alustetaan luokan listview olio.
        dateList = (ListView)findViewById(R.id.list);
        dateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dateListObjects.length > position) {
                    Intent LinechartActivity = new Intent(getApplicationContext(), TemperatureChart.class);
                    LinechartActivity.putExtra("dateString", dateListObjects[position].dateString);
                    startActivity(LinechartActivity);
                }
            }
        });

        if(savedInstanceState != null)
        {
            // Päivitetään lista
            new POSTRequestAsync().execute();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Päivitetään lista
        new POSTRequestAsync().execute();
    }

    public class POSTRequestAsync extends AsyncTask<Void, Void, DateListObject[]>
    {
        private ProgressDialog progressDialog;
        private Boolean ConnectionOK = false;

        protected void onPreExecute()
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
            this.progressDialog = new ProgressDialog(context);
            this.progressDialog.setTitle("Ladataan");
            this.progressDialog.setCancelable(false);
            this.progressDialog.setMessage("Ladataan dataa palvelimelta:\n" + pref.getString("domain", ""));
            this.progressDialog.show();
        }

        protected void onPostExecute(DateListObject[] objects)
        {
            this.progressDialog.dismiss();

            // Asetetaan saatu data pääikkunan listviewiin
            if(objects != null)
            {
                // Järjestetään lista ja tallennetaan se pääikkunan staattiseen muuttujaan talteen
                for (int i = 0; i < objects.length; i++)
                {
                    for (int j = 0; j < objects.length; j++)
                    {
                        if(objects[i].date.compareTo(objects[j].date) >= 0)
                        {
                            DateListObject o = objects[i];
                            objects[i] = objects[j];
                            objects[j] = o;
                        }
                    }
                }

                MainWindow.dateListObjects = objects;
                MainWindow.dateList.setAdapter(new DateList(MainWindow.context, objects));

            }
            else
            {
                if(this.ConnectionOK == true)
                {
                    Toast.makeText(MainWindow.context, "Käyttäjätunnus/salasana on väärä\ntai dataa ei ole saatavilla.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainWindow.context, "Palvelimeen ei saatu yhteyttä.", Toast.LENGTH_LONG).show();
                }
            }
        }

        protected DateListObject[] doInBackground(Void... a)
        {
            // Avataan preference ja luetaan sinne asetettu tieto yhdistämistä varten
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
            String username = pref.getString("username", "");
            String password = pref.getString("passwd", "");
            String domain = pref.getString("domain", "");

            // Tarkistetaan että asetukset on asetettu
            if((username == null || username == "") || (password == null || password == "") || (domain == null || domain == ""))
            {
                return null;
            }

            // Kootaan kysely asetuksiin määritellyillä tunnuksilla
            String ipaddress = "";
            try
            {
                String q = "username=" + username + "&password=" + password + "&function=listdates";
                DateListObject[] data = SendQuery(domain, q);

                this.ConnectionOK = true;
                return data;
            }
            catch (Exception e)
            {
                this.ConnectionOK = false;
                e.printStackTrace();
            }
            return null;
        }

        // Lähetetään POST-kysely palvelimelle
        private DateListObject[] SendQuery(String Url, String params) throws IOException
        {
            String urlString = "";
            try
            {
                // Parsitaan saatu osoite IP-osoitteeksi
                String[] UrlSplitted = Url.split("//")[1].split("/");
                urlString = "http://" + InetAddress.getByName(UrlSplitted[0]).toString().split("/")[1];
                for (int i = 1; i < UrlSplitted.length; i++)
                {
                    urlString += "/" + UrlSplitted[i];
                }
                urlString += "/";
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                return null;
            }

            // Tarkistetaan että osoite saatiin väännettyä ip-muotoon (Dynaamisten uudelleenohjauspalveluiden vuoksi)
            if(urlString != "")
            {
                // Avataan yhteys ja asetetaan yhteys kirjoitustilaan
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setDoOutput(true);

                // Lähetetään POST-kysely palvelimelle
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(params);
                out.close();

                // Luetaan vastaus palvelimelta
                String response = "";
                Scanner inStream = new Scanner(conn.getInputStream());
                while(inStream.hasNextLine())
                {
                    response += (inStream.nextLine());
                }
                inStream.close();

                if(response != "" && response != null)
                {
                    DateListObject[] dateListObjects;
                    try
                    {
                        JSONArray jsonArray = new JSONArray(response);
                        dateListObjects = new DateListObject[jsonArray.length()];

                        // Parsitaan JSON-dataa
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsobj = jsonArray.getJSONObject(i);
                            String date = jsobj.getString("date");
                            String inavg = jsobj.getString("inavg");
                            String outavg = jsobj.getString("outavg");
                            DateListObject dateObj = new DateListObject(date, inavg, outavg);
                            dateListObjects[i] = dateObj;
                        }
                        // Palautetaan taulukko kutsujalle
                        return dateListObjects;
                    }
                    catch (JSONException e) {e.printStackTrace();}
                }
            }
            return null;
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

            case R.id.update:
                    // Päivitetään lista pääikkunaan
                    new POSTRequestAsync().execute();
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
