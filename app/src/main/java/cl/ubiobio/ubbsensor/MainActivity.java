package cl.ubiobio.ubbsensor;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    //Variables globales para almacenar los datos de los sensores
    private String temActual;
    private int temProm;
    private int radActual;
    private int radProm;
    private int humActual;
    private int humProm;

    //Texto a editar en el layout
    private TextView editTemp;

    //Tokens para hacer la conexion con la API
    private String tokenAcceso = "SYstxy8h0a";
    private String tokenTemp = "E1yGxKAcrg";
    private String tokenRad = "8IvrZCP3qa";
    private String tokenHum = "VIbSnGKyLW";

    //variables ocupadas para almacenar la fecha actual como Date y luego transformarla y almacenarla en String
    private Date fechaActual = new Date();
    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy"); //deja la fecha actual en el formato aceptado por la API
        fecha= dateFormat.format(fechaActual);
        Log.d("LOG_WS", "Fecha: "+fecha);
        obtenerTemperatura(tokenAcceso,tokenTemp,fecha);
        editTemp = findViewById(R.id.temActual);
        editTemp.setText(temActual);
    }

    //funcion que obtiene los valores del sensor de temperatura

    private void obtenerTemperatura(final String tokenAcces, final String tokenSensor, final String fechaHoy){
        Log.d("LOG WS", "entre");
        String WS_URL = "http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+tokenAcces+"/"+tokenSensor+"/"+fechaHoy;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(
                Request.Method.GET,
                WS_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.d("LOG WS", response);
                        try {
                            Log.d("LOG WS", "entre al try");
                            JSONObject responseJson = new JSONObject(response);
                            Log.d("LOG WS", "fecha: " + responseJson.getJSONObject("data").getString("fecha"));
                            Log.d("LOG WS", "hora: "+ responseJson.getJSONObject("data").getString("hora"));
                            Log.d("LOG WS", "valor: "+ responseJson.getJSONObject("data").getString("valor"));
                            temActual = responseJson.getJSONObject("data").getString("valor");
                            //generateToast(responseJson.getString("info"));
                            if(responseJson.getBoolean("resp")){
                                //iniciar otra actividad.....
                                //final String login = responseJson.getJSONObject("data").getString("nombres");
                            }else{
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG WS", error.toString());
                //generateToast("Error en el WEB Service");
            }
        }
        ){
           @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("TokenAcceso",tokenAcces);
                params.put("TokenSensor",tokenTemp);
                params.put("Fecha",fechaHoy);
                return params;
            }
        };
        requestQueue.add(request);
    }
}
