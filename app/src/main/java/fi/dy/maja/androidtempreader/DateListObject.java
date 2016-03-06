package fi.dy.maja.androidtempreader;

import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Jarno on 2.3.2016.
 */
public class DateListObject
{
    public String dateString;
    public Date date;
    public double inAvg;
    public double outAvg;

    public DateListObject(String date, String inAvg, String outAvg)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            this.date = df.parse(date);
        }
        catch (ParseException e) {e.printStackTrace();}

        this.inAvg = Double.parseDouble(inAvg);
        this.outAvg = Double.parseDouble(outAvg);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        this.dateString = sdf.format(this.date);
    }

    public String getOutAvg()
    {
        return String.format("%.2f", this.outAvg) + "\u2103";
    }

    public String getInAvg()
    {
        return String.format("%.2f", this.inAvg) + "\u2103";
    }

    // Palautetaan päivämäärä oikeassa muodossa
    public String getDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        return df.format(this.date);
    }
}
