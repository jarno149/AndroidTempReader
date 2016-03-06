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
public class DateList extends ArrayAdapter<DateListObject>
{
    private final Context context;
    private final DateListObject[] values;

    public DateList(Context context, DateListObject[] values)
    {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.mylistlayout, parent, false);

        TextView dateLine = (TextView)rowView.findViewById(R.id.date);
        TextView outTemp = (TextView) rowView.findViewById(R.id.outTemp);
        TextView inTemp = (TextView) rowView.findViewById(R.id.inTemp);

        dateLine.setText(values[position].getDate());
        outTemp.setText(values[position].getOutAvg());
        inTemp.setText(values[position].getInAvg());

        return rowView;
    }


}
