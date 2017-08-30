package com.simple.bindview;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.simple.BindView;
import com.simple.OnClick;
import com.simple.lib.BindViewLib;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.button)
    Button mButton;

    @OnClick(R.id.button)
    void submit() {
        Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewLib.bind(this);
        mTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
    }
}
