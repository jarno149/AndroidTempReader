package fi.dy.maja.androidtempreader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class TemperatureChart extends AppCompatActivity {

    private String dateString;

    // Luodaan context objekti jotta voidaan piirtää latausikkuna tähän activityyn
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);

        this.context = this;
            Bundle extras = getIntent().getExtras();
            if(extras != null)
            {
                this.dateString = extras.getString("dateString");
                new POSTRequestAsync().execute(this.dateString);
            }
            else
            {
                Toast.makeText(getApplicationContext() ,"Dataa ei löydy", Toast.LENGTH_LONG).show();
            }
        }


    // Haetaan data palvelimelta
    public class POSTRequestAsync extends AsyncTask<String, Void, MeasurementDate>
    {
        private ProgressDialog progressDialog;

        protected void onPreExecute()
        {
            // Luetaan tallennettu palvelimen nimi ja näytetään käyttäjälle tieto että dataa haetaan palvelimelta
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainWindow.context);
            this.progressDialog = new ProgressDialog(context);
            this.progressDialog.setTitle("Ladataan");
            this.progressDialog.setCancelable(false);
            this.progressDialog.setMessage("Ladataan dataa palvelimelta:\n" + pref.getString("domain", ""));
            this.progressDialog.show();
        }

        protected void onPostExecute(MeasurementDate md)
        {
            this.progressDialog.dismiss();

            // Luodaan uusi valueformatter
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return value + "\u2103";
                }
            };

            // Luodaan uusi LineChart
            LineChart linechart = new LineChart(getBaseContext());
            setContentView(linechart);

            // Ulkolämpötilat
            ArrayList<Entry> outTemps = new ArrayList<>();
            for (int i = 0; i < md.outTemps.length; i++)
            {
                outTemps.add(new Entry((float)md.outTemps[i].temperature, i));
            }

            // Sisälämpötilat
            ArrayList<Entry> inTemps = new ArrayList<>();
            for (int i = 0; i < md.inTemps.length; i++)
            {
                inTemps.add(new Entry((float)md.inTemps[i].temperature, i));
            }

            // Luodaan interface lista dataseteistä
            ArrayList<ILineDataSet> lines = new ArrayList<>();
            // Luodaan ulkolämpötiloista LineDataSet-olio
            LineDataSet OutDataset = new LineDataSet(outTemps, "Ulkolämpötilat");
            OutDataset.setColor(Color.RED);
            OutDataset.setCircleColor(Color.RED);
            OutDataset.setValueFormatter(formatter);
            OutDataset.setValueTextSize(8);

            LineDataSet inDataset = new LineDataSet(inTemps, "Sisälämpötilat");
            inDataset.setColor(Color.GREEN);
            inDataset.setCircleColor(Color.GREEN);
            inDataset.setValueFormatter(formatter);
            inDataset.setValueTextSize(8);

            // Lisätään ulkolämpötilojen dataset listaan
            lines.add(OutDataset);
            // Lisätään sisälämpötilojen dataset listaan
            lines.add(inDataset);

            // Luodaan X-akselin labelit
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < md.outTemps.length; i++)
            {
                labels.add(md.outTemps[i].timeString);
            }

            // Asetetaan data Charttiin ja muutetaan sen asetuksia
            LineData data = new LineData(labels, lines);
            linechart.setData(data);
            XAxis xAxis = linechart.getXAxis();
            xAxis.setTextSize(20);
            Legend legend = linechart.getLegend();
            legend.setTextSize(15);
            YAxis yAxis1 = linechart.getAxisLeft();
            YAxis yAxis2 = linechart.getAxisRight();
            yAxis1.setTextSize(15);
            yAxis2.setTextSize(15);

            // Animoidaan X-akselin datan piirto
            linechart.animateX(1500);
        }

        protected MeasurementDate doInBackground(String... date)
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
                String q = "username=" + username + "&password=" + password + "&date=" + date[0];
                MeasurementDate md = SendQuery(domain, q);

                return md;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        // Lähetetään POST-kysely palvelimelle
        private MeasurementDate SendQuery(String Url, String params) throws IOException
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

                // Parsitaan data kasaan JSONista
                if(response != "" && response != null)
                {
                    MeasurementDate measurementDate = new MeasurementDate();
                    try
                    {
                        int pos = response.indexOf('[', 1);
                        JSONArray array1 = new JSONArray(response.substring(0, pos));
                        JSONArray array2 = new JSONArray(response.substring(pos));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        measurementDate.date = sdf.parse(array1.getJSONObject(0).getString("date"));

                        Measurement[] outTemps = new Measurement[array1.length()];
                        Measurement[] inTemps = new Measurement[array2.length()];

                        for (int i = 0; i < array1.length(); i++)
                        {
                            String time = array1.getJSONObject(i).getString("time");
                            double temperature = array1.getJSONObject(i).getDouble("temperature");
                            String location = array1.getJSONObject(i).getString("location");

                            outTemps[i] = new Measurement(location, temperature, time);
                        }
                        for (int i = 0; i < array2.length(); i++)
                        {
                            String time = array2.getJSONObject(i).getString("time");
                            double temperature = array2.getJSONObject(i).getDouble("temperature");
                            String location = array2.getJSONObject(i).getString("location");

                            inTemps[i] = new Measurement(location, temperature, time);
                        }

                        measurementDate.inTemps = inTemps;
                        measurementDate.outTemps = outTemps;

                        return measurementDate;
                    }
                    catch (Exception e) {e.printStackTrace();}
                }
            }
            return null;
        }
    }
}
