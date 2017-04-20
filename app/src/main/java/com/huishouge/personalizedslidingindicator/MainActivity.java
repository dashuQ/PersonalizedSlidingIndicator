package com.huishouge.personalizedslidingindicator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.huishouge.personalizedslidingindicator.view.PersoSlidiIndicListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PersoSlidiIndicListView.OnPositionChangedListener {

    @BindView(R.id.lv)
    PersoSlidiIndicListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initPersoSlidiIndicListView();

        refreshPersoSlidiIndicListView();

    }

    private void refreshPersoSlidiIndicListView() {
        int s = 100;
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < s; i++) {
            list.add(i + "");
        }
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
    }

    private void initPersoSlidiIndicListView() {
        lv.setOnPositionChangedListener(this);
    }

    @Override
    public void onPositionChanged(PersoSlidiIndicListView listview, int position, View scrollBarPanel) {
        ((TextView) scrollBarPanel).setText("" + position);
    }
}
