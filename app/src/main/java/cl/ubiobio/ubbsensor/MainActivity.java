package cl.ubiobio.ubbsensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btemp;
    private Button bhum;
    private Button brad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btemp =findViewById(R.id.tempeButton);
        bhum = findViewById(R.id.humeButton);
        brad = findViewById(R.id.radButton);
        btemp.setOnClickListener(this);
        bhum.setOnClickListener(this);
        brad.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //Si selecciono el boton temperatura me dirijo al layout activity_temperatura.xml
            case R.id.tempeButton:
                Intent tempe = new Intent(MainActivity.this, Temperatura.class);
                startActivity(tempe);
                break;
            //Si selecciono el boton humedad me dirijo al layout activity_humedad.xml
            case R.id.humeButton:
                Intent humed = new Intent(MainActivity.this, Humedad.class);
                startActivity(humed);
                break;
            //Si selecciono el boton radiacion me dirijo al layout activity_radiacion.xml
            case R.id.radButton:
                Intent radi = new Intent(MainActivity.this, Radiacion.class);
                startActivity(radi);
                break;
        }
    }
}
