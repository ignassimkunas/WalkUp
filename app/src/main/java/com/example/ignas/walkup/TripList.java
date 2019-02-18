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

        TextView distance = listViewItem.findViewById(R.id.textView);

        distance.setText(Double.toString(tripsList.get(position).distance));

        return listViewItem;
    }
}
