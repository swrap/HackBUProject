package com.interaxon.test.libmuse;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class game extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Activity a = getParent();
        TextView t = (TextView)findViewById(R.id.gameInt);
        t.setText("" + temperoony);
    }

}
