package com.simple.bindview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.simple.BindView;
import com.simple.lib.BindViewLib;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.button)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewLib.bind(this);
        mTextView.setText("Nguyen Thanh Tuan");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "Simple", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
