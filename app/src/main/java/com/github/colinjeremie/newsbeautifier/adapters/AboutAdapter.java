package com.github.colinjeremie.newsbeautifier.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.colinjeremie.newsbeautifier.R;

/**
 * The Adapter used for the about list in {@link com.github.colinjeremie.newsbeautifier.activities.AboutActivity}
 *
 * Created by jerem_000 on 2/26/2016.
 */
public class AboutAdapter extends ArrayAdapter<AboutAdapter.Model> {
    public AboutAdapter(Context context) {
        super(context, R.layout.about_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = convertView == null ? null : (ViewHolder) convertView.getTag();

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.about_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.about_title);
            viewHolder.subTitle = (TextView) convertView.findViewById(R.id.about_subtitle);

            convertView.setTag(viewHolder);
        }

        Model item = getItem(position);

        viewHolder.title.setText(item.name);
        viewHolder.subTitle.setText(item.value);

        return convertView;
    }

    public class ViewHolder {
        public TextView title;
        public TextView subTitle;
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