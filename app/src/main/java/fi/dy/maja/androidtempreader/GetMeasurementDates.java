package fi.dy.maja.androidtempreader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Created by Jarno on 28.2.2016.
 */


public class GetMeasurementDates extends AsyncTask<String, Void, String>
{
    protected String doInBackground(String... query)
    {
        // Avataan preference ja luetaan sinne asetettu tieto yhdistämistä varten
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
        String username = pref.getString("username", "");
        String password = pref.getString("passwd", "");
        String domain = pref.getString("domain", "");

        // Tarkistetaan että asetukset on asetettu
        if((username == null || username == "") || (password == null || password == "") || (domain == null || domain == ""))
        {
            MainWindow.DataIsSet = false;
            return null;
        }

        // Kootaan kysely asetuksiin määritellyillä tunnuksilla
        String ipaddress = "";
        try
        {
            String q = "username=" + username + "&password=" + password + "&" + query[0];
            String data = SendQuery(domain, q);
            // Lähetetään tieto pääikkunaan
            return data;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    // Lähetetään POST-kysely palvelimelle
    private String SendQuery(String Url, String params) throws IOException
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

            // Palautetaan tulos tekstijonona
            if(response == "")
            {
                return "no response data";
            }
            else
            {
                return response;
            }
        }
        return null;
    }
}
