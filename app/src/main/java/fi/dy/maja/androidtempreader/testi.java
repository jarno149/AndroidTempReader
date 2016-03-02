package fi.dy.maja.androidtempreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Jarno on 1.3.2016.
 */
public class testi extends ArrayAdapter<String>
{
    private final Context context;
    private final String[] values;

    public testi(Context context, String[] values)
    {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.testi, parent, false);

        TextView dateLine = (TextView)rowView.findViewById(R.id.firstLine);
        TextView secLeft = (TextView) rowView.findViewById(R.id.secondLeft);
        TextView secRight = (TextView) rowView.findViewById(R.id.secondRight);

        String[] data = values[position].split("#");

        dateLine.setText(data[0]);
        secLeft.setText(data[1]);
        secRight.setText(data[2]);

        return rowView;
    }
}
