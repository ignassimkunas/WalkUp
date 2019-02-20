package com.example.ignas.walkup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TripList extends ArrayAdapter<Trips> {

    private Activity context;
    private List<Trips> tripsList;

    public TripList(Activity context, List<Trips> tripsList) {

        super(context, R.layout.list_layout, tripsList);
        this.context = context;
        this.tripsList = tripsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView distance = listViewItem.findViewById(R.id.distance);
        TextView time = listViewItem.findViewById(R.id.time);
        TextView avgSpeed = listViewItem.findViewById(R.id.avg_speed);

        long minutes = tripsList.get(position).time / 60;
        long seconds = tripsList.get(position).time - minutes * 60;

        String secondString = Long.toString(seconds);

        if (seconds < 10) {
            secondString = "0" + Long.toString(seconds);
        }

        time.setText(Long.toString(minutes) + ":" + secondString);

        distance.setText(Double.toString(tripsList.get(position).distance)+ "km");
        avgSpeed.setText(Double.toString(tripsList.get(position).avgSpeed)+"km/h");

        return listViewItem;
    }
}
