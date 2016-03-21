package com.github.colinjeremie.newsbeautifier.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.colinjeremie.newsbeautifier.R;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/26/2016.
 */
public class AboutAdapter extends ArrayAdapter<AboutAdapter.Model> {
    public AboutAdapter(Context context) {
        super(context, R.layout.about_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.about_item, parent, false);
        }

        Model item = getItem(position);

        ((TextView)convertView.findViewById(R.id.about_title)).setText(item.name);
        ((TextView)convertView.findViewById(R.id.about_subtitle)).setText(item.value);

        return convertView;
    }

    public static class Model {
        public final String name;
        public final String value;

        public Model(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}