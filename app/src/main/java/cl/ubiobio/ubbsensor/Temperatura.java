package cl.ubiobio.ubbsensor;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.ImageSpeedometer;
import com.github.anastr.speedviewlib.Speedometer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Temperatura extends AppCompatActivity {

    ImageSpeedometer gaugeTempMax;
    ImageSpeedometer gaugeTempMin;
    ImageSpeedometer gaugeTempProm;

    //Titulo del ActionBar
    private TextView actionBarTitle;

    //variables utilizadas para desplegar fecha
    private static final String TAG = "MainActivity";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String dia;
    private String mes;

    //arreglo para almacenar todas las temperaturas de un dia determinado
    private ArrayList<Integer> promedioTemp;

    //Variables globales para almacenar los datos de los sensores
    private int temMin;
    private int temMax;
    private Float temProm;

    //Tokens para hacer la conexion con la API
    private String tokenAcceso = "mhv8o25C7Q";
    private String tokenTemp = "E1yGxKAcrg";

    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperatura);
        //cambio el ActionBar por uno personalizado (centrado)
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        actionBarTitle = (TextView) findViewById(R.id.customActionBar);
        actionBarTitle.setText("Temperatura");

        temProm=Float.valueOf(0);

        //inicializo cada gauge (min, max, prom)
        gaugeTempMax = findViewById(R.id.gaugeTempMax);
        gaugeTempMin = findViewById(R.id.gaugeTempMin);
        gaugeTempProm = findViewById(R.id.gaugeTempProm);

        //codigo para desplegar las fechas
        mDisplayDate = (TextView) findViewById(R.id.tvDateTemp);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Temperatura.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                //transformo el dia y el mes a String para poder colocarles un 0 antes del numero en el caso de que sean menores a 10 (para seguir el formato de la API)
                dia = String.valueOf(day);
                mes = String.valueOf(month);

                if(day <10){
                    dia = "0"+day;
                }
                if(month <10){
                    mes = "0"+month;
                }
                //Muestro la fecha seleccionada en el layout
                String date = dia+ "/" + mes + "/" + year;
                mDisplayDate.setText(date);
                //transformo la fecha a ddmmyyyy para hacer la consulta a la API
                fecha= dia+""+mes+""+year;
                //fecha="26052018";
                Log.d("LOG_WS", "Fecha: "+fecha);
                //vacio el arreglo de temperaturas
                promedioTemp.clear();

                //Inicio el metodo que consulta a la API segun la fecha entregada por parametro y setea las temperaturas de la pantalla
                obtenerTemperatura(tokenAcceso,tokenTemp,fecha);
            }
        };

        //inicializo el arreglo donde almacenare la temperatura
        promedioTemp = new ArrayList<>();



    }

    private void generateToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
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
                        try {
                            JSONObject responseJs = new JSONObject(response);
                            JSONArray responseJson = responseJs.getJSONArray("data");

                            for(int i = 0; i < responseJson.length(); i++){
                                JSONObject o = responseJson.getJSONObject(i);

                                try{
                                    //Para la primera vuelta establesco el valor de max y min de la temperatura como el primer valor del dia
                                    if(i==0){
                                        temMin = Integer.parseInt(o.getString("valor"));
                                        temMax = Integer.parseInt(o.getString("valor"));
                                        promedioTemp.add(Integer.parseInt(o.getString("valor")));
                                    }

                                    //Si la temperatura de esta iteracion es menor que temMin, temMin obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))<temMin)){
                                        temMin = Integer.parseInt(o.getString("valor"));
                                    }

                                    //Si la temperatura de esta iteracion es mayor que temMax, temMax obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))>temMax)){
                                        temMax = Integer.parseInt(o.getString("valor"));
                                    }
                                    promedioTemp.add(Integer.parseInt(o.getString("valor")));

                                    Log.d("LOG D: ","PRIMER MIN: "+temMin);
                                    Log.d("LOG D: ","PRIMER MAX: "+temMax);

                                }catch (NumberFormatException e){

                                }
                            }

                            //recorro too el arreglo de temperatura, para sumar los valores

                            temProm= Float.valueOf(0);
                            for(int j=0;j<promedioTemp.size()-1;j++){
                                temProm += Float.valueOf(promedioTemp.get(j));
                            }
                            //divido la suma de todas las temperaturas por el tamaño del arreglo
                            temProm = temProm/Float.valueOf(promedioTemp.size()-1);

                            //seteo los valores de temperatura maxima, minima y promedio del gauge
                            gaugeTempMax.speedTo(temMax);
                            gaugeTempMin.speedTo(temMin);
                            gaugeTempProm.speedTo(Math.round(temProm));



                        } catch (JSONException e) {
                            //si hay un error los seteo en 0
                            gaugeTempMax.speedTo(-10);
                            gaugeTempMin.speedTo(-10);
                            gaugeTempProm.speedTo(-10);

                            /*Existe el caso en el que la API devuelve un JSON como objeto y no como array (cuando la conexion es exitosa, pero no
                            existen mediciones), en ese caso envio el siguiente mensaje*/
                            generateToast("Error: No existen datos para la fecha seleccionada");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //si hay un error los seteo en 0
                gaugeTempMax.speedTo(-10);
                gaugeTempMin.speedTo(-10);
                gaugeTempProm.speedTo(-10);

                //cuando la fecha ingresada es superior a la actual envio el siguiente mensaje
                Log.d("LOG WS", error.toString());
                generateToast("Error: No existen datos para la fecha seleccionada");
            }
        }
        );
        requestQueue.add(request);
    }

}
