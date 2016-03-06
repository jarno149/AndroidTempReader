package fi.dy.maja.androidtempreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

/**
 * Created by Jarno on 6.3.2016.
 */
public class Measurement
{
    public String location;
    public double temperature;
    public Date time;
    public String timeString;

    public Measurement(String location, double temperature, String time)
    {
        this.location = location;
        this.temperature = temperature;
        this.timeString = time;
        SimpleDateFormat sdf = new SimpleDateFormat("kk.mm.ss");
        try
        {
            this.time = sdf.parse(time);
        }
        catch (ParseException e) {e.printStackTrace();}
    }
}
