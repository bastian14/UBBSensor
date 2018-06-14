package cl.ubiobio.ubbsensor;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.ImageLinearGauge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Radiacion extends AppCompatActivity {

    ImageLinearGauge gaugeRadMax;
    ImageLinearGauge gaugeRadMin;
    ImageLinearGauge gaugeRadProm;

    //Titulo del ActionBar
    private TextView actionBarTitle;

    //variables utilizadas para desplegar fecha
    private static final String TAG = "MainActivity";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String dia;
    private String mes;

    //arreglo para almacenar todas las radiaciones de un dia determinado
    private ArrayList<Integer> promedioRad;

    //Variables globales para almacenar los datos de los sensores
    private int radMin;
    private int radMax;
    private Float radProm;

    //Tokens para hacer la conexion con la API
    private String tokenAcceso = "mhv8o25C7Q";
    private String tokenRad = "8IvrZCP3qa";

    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiacion);
        //cambio el ActionBar por uno personalizado (centrado)
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        actionBarTitle = (TextView) findViewById(R.id.customActionBar);
        actionBarTitle.setText("Radiación");

        radProm=Float.valueOf(0);

        //inicializo cada gauge (min, max, prom)
        gaugeRadMax = findViewById(R.id.gaugeRadMax);
        gaugeRadMin = findViewById(R.id.gaugeRadMin);
        gaugeRadProm = findViewById(R.id.gaugeRadProm);

        //codigo para desplegar las fechas
        mDisplayDate = (TextView) findViewById(R.id.tvDateRad);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Radiacion.this,
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
                //vacio el arreglo de radiaciones
                promedioRad.clear();

                //Inicio el metodo que consulta a la API segun la fecha entregada por parametro y setea las radiaciones de la pantalla
                obtenerRadiacion(tokenAcceso,tokenRad,fecha);
            }
        };

        //inicializo el arreglo donde almacenare la radiacion
        promedioRad = new ArrayList<>();
    }

    private void generateToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    //funcion que obtiene los valores del sensor de radiacion

    private void obtenerRadiacion(final String tokenAcces, final String tokenSensor, final String fechaHoy){

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

                                Log.d("Fecha: ",o.getString("fecha"));
                                Log.d("Hora: ",o.getString("hora"));
                                Log.d("Valor: ",o.getString("valor"));

                                try{
                                    //Para la primera vuelta establesco el valor de max y min de la radiacion como el primer valor del dia
                                    if(i==0){
                                        radMin = Integer.parseInt(o.getString("valor"));
                                        radMax = Integer.parseInt(o.getString("valor"));
                                        promedioRad.add(Integer.parseInt(o.getString("valor")));
                                    }

                                    //Si la radiacion de esta iteracion es menor que radMin, radMin obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))<radMin)){
                                        radMin = Integer.parseInt(o.getString("valor"));
                                    }

                                    //Si la radiacion de esta iteracion es mayor que radMax, radMax obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))>radMax)){
                                        radMax = Integer.parseInt(o.getString("valor"));
                                    }
                                    promedioRad.add(Integer.parseInt(o.getString("valor")));

                                    Log.d("LOG D: ","PRIMER MIN: "+radMin);
                                    Log.d("LOG D: ","PRIMER MAX: "+radMax);

                                }catch (NumberFormatException e){

                                }
                            }

                            //recorro too el arreglo de radiacion, para sumar los valores

                            radProm= Float.valueOf(0);
                            for(int j=0;j<promedioRad.size()-1;j++){
                                radProm += Float.valueOf(promedioRad.get(j));
                            }
                            //divido la suma de todas las radiaciones por el tamaño del arreglo
                            radProm = radProm/Float.valueOf(promedioRad.size()-1);

                            //seteo los valores de radiacion maxima, minima y promedio del layout
                            gaugeRadMax.speedTo(radMax);
                            gaugeRadMin.speedTo(radMin);
                            gaugeRadProm.speedTo(Math.round(radProm));

                        } catch (JSONException e) {
                            //si hay un error los seteo en 0
                            gaugeRadMax.speedTo(0);
                            gaugeRadMin.speedTo(0);
                            gaugeRadProm.speedTo(0);

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
                gaugeRadMax.speedTo(0);
                gaugeRadMin.speedTo(0);
                gaugeRadProm.speedTo(0);

                //cuando la fecha ingresada es superior a la actual envio el siguiente mensaje
                Log.d("LOG WS", error.toString());
                generateToast("Error: No existen datos para la fecha seleccionada");
            }
        }
        );
        requestQueue.add(request);
    }
}
