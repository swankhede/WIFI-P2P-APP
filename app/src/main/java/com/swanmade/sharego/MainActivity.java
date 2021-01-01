package com.swanmade.sharego;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.swanmade.sharego.MainActivity;
import com.swanmade.sharego.R;

public class MainActivity extends AppCompatActivity {
    Button button;
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.splash_screen );
        button = (Button)findViewById ( R.id.start );
        button.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick (View view){
                openHomescreen();
            }
        } );

    }

    private void openHomescreen (){
        Intent intent = new Intent ( this, MainActivity2.class );
        startActivity ( intent );

    }
}