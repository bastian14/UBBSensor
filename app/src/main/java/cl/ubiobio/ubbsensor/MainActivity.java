package cl.ubiobio.ubbsensor;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private ArrayList<Float> promedio;

    private Map<String, String> params;

    //Variables globales para almacenar los datos de los sensores
    private Float temMin;
    private Float temMax;
    private Float temProm;
    private Float sumaProm;
    private int radActual;
    private int radProm;
    private int humActual;
    private int humProm;

    //Texto a editar en el layout
    private TextView editTemp;

    //Tokens para hacer la conexion con la API
    private String tokenAcceso = "mhv8o25C7Q";
    private String tokenTemp = "E1yGxKAcrg";
    private String tokenRad = "8IvrZCP3qa";
    private String tokenHum = "VIbSnGKyLW";

    //variables ocupadas para almacenar la fecha actual como Date y luego transformarla y almacenarla en String
    private Date fechaActual = new Date();
    private String fecha;

    private ArrayList<Datos> mediciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediciones = new ArrayList<>();
        promedio = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy"); //deja la fecha actual en el formato aceptado por la API
        //fecha= dateFormat.format(fechaActual);
        fecha="26052018";
        Log.d("LOG_WS", "Fecha: "+fecha);
        obtenerTemperatura(tokenAcceso,tokenTemp,fecha);
        editTemp = findViewById(R.id.temActual);
        //editTemp.setText(temActual);
    }

    private void generateToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    //funcion que obtiene los valores del sensor de temperatura

    private void obtenerTemperatura(final String tokenAcces, final String tokenSensor, final String fechaHoy){ //sacar throws JSONException

        Log.d("LOG WS", "entre");
        String WS_URL = "http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+tokenAcces+"/"+tokenSensor+"/"+fechaHoy;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(
                Request.Method.GET,
                WS_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJs = new JSONObject(response);

                            JSONArray responseJson = responseJs.getJSONArray("data");

                            for(int i = 0; i < responseJson.length(); i++){
                                //JSONObject o = responseJson.getJSONObject(i);
                                JSONObject o = responseJson.getJSONObject(i);
                                Datos dat = new Datos();

                                Log.d("Fecha: ",o.getString("fecha"));
                                Log.d("Hora: ",o.getString("hora"));
                                Log.d("Valor: ",o.getString("valor"));

                                dat.setFecha(o.getString("fecha"));
                                dat.setHora(o.getString("hora"));
                                dat.setValor(o.getString("valor"));

                                try{
                                    /*far.setLatitud(Double.parseDouble(lat));
                                    far.setLongitud(Double.parseDouble(lng));*/

                                    //Para la primera vuelta establesco el valor de max y min de la temperatura como el primer valor del dia
                                    if(i==0){
                                        temMin = Float.parseFloat(o.getString("valor"));
                                        temMax = Float.parseFloat(o.getString("valor"));
                                        promedio.add(Float.parseFloat(o.getString("valor")));
                                    }

                                    //Si la temperatura de esta iteracion es menor que temMin, temMin obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))<temMin)){
                                        temMin = Float.parseFloat(o.getString("valor"));
                                    }

                                    //Si la temperatura de esta iteracion es mayor que temMax, temMax obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))>temMax)){
                                        temMax = Float.parseFloat(o.getString("valor"));
                                    }






                                }catch (NumberFormatException e){
                                   /* far.setLatitud(0);
                                    far.setLongitud(0);*/
                                }

                                mediciones.add(dat);




                            }

                            for(int i=0;i<promedio.size()-1;i++){
                                temProm += promedio.get(i);
                            }
                            temProm = temProm/promedio.size();

                            //Log.d("LOG", "cantidad: " + farmaciasDeTurno.size());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG WS", error.toString());
                generateToast("Error en el WEB Service");
            }
        }
        );
        requestQueue.add(request);
    }

}
