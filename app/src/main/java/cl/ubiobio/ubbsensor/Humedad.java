package cl.ubiobio.ubbsensor;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class Humedad extends AppCompatActivity {

    //variables utilizadas para desplegar fecha
    private static final String TAG = "MainActivity";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String dia;
    private String mes;

    //arreglo para almacenar todas las mediciones de humedad de un dia determinado
    private ArrayList<Integer> promedioHum;

    //Variables globales para almacenar los datos de los sensores
    private int humMin;
    private int humMax;
    private Float humProm;

    //Texto a editar en el layout
    private TextView editHumMin;
    private TextView editHumMax;
    private TextView editHumProm;

    //Tokens para hacer la conexion con la API
    private String tokenAcceso = "mhv8o25C7Q";
    private String tokenHum = "VIbSnGKyLW";

    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humedad);

        humProm=Float.valueOf(0);
        //codigo para desplegar las fechas
        mDisplayDate = (TextView) findViewById(R.id.tvDateHum);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Humedad.this,
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
                //vacio arreglo de humedades
                promedioHum.clear();

                //Inicio el metodo que consulta a la API segun la fecha entregada por parametro y setea los datos de humedad de la pantalla
                obtenerHumedad(tokenAcceso,tokenHum,fecha);
            }
        };

        //inicializo el arreglo donde almacenare la humedad
        promedioHum = new ArrayList<>();
    }

    private void generateToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    //funcion que obtiene los valores del sensor de humedad

    private void obtenerHumedad(final String tokenAcces, final String tokenSensor, final String fechaHoy){

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
                                Datos dat = new Datos();

                                Log.d("Fecha: ",o.getString("fecha"));
                                Log.d("Hora: ",o.getString("hora"));
                                Log.d("Valor: ",o.getString("valor"));

                                dat.setFecha(o.getString("fecha"));
                                dat.setHora(o.getString("hora"));
                                dat.setValor(o.getString("valor"));

                                try{
                                    //Para la primera vuelta establesco el valor de max y min de la humedad como el primer valor del dia
                                    if(i==0){
                                        humMin = Integer.parseInt(o.getString("valor"));
                                        humMax = Integer.parseInt(o.getString("valor"));
                                        promedioHum.add(Integer.parseInt(o.getString("valor")));
                                    }

                                    //Si la humedad de esta iteracion es menor que humMin, humMin obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))<humMin)){
                                        humMin = Integer.parseInt(o.getString("valor"));
                                    }

                                    //Si la humedad de esta iteracion es mayor que humMax, humMax obtiene el valor de la iteracion
                                    if((Integer.parseInt(o.getString("valor"))>humMax)){
                                        humMax = Integer.parseInt(o.getString("valor"));
                                    }
                                    promedioHum.add(Integer.parseInt(o.getString("valor")));

                                    Log.d("LOG D: ","PRIMER MIN: "+humMin);
                                    Log.d("LOG D: ","PRIMER MAX: "+humMax);

                                }catch (NumberFormatException e){

                                }
                            }

                            //recorro too el arreglo de humedades, para sumar los valores

                            humProm= Float.valueOf(0);
                            for(int j=0;j<promedioHum.size()-1;j++){
                                humProm += Float.valueOf(promedioHum.get(j));
                            }
                            //divido la suma de todas las humedades por el tamaño del arreglo
                            humProm = humProm/Float.valueOf(promedioHum.size()-1);

                            //seteo los valores de humedad maxima, minima y promedio del layout
                            editHumMin = findViewById(R.id.humMin);
                            editHumMin.setText(String.valueOf(humMin));
                            editHumMax = findViewById(R.id.humMax);
                            editHumMax.setText(String.valueOf(humMax));
                            editHumProm = findViewById(R.id.humProm);
                            editHumProm.setText(String.valueOf(humProm));

                        } catch (JSONException e) {
                            //si hay un error los seteo en 0
                            editHumMin = findViewById(R.id.humMin);
                            editHumMin.setText("0");
                            editHumMax = findViewById(R.id.humMax);
                            editHumMax.setText("0");
                            editHumProm = findViewById(R.id.humProm);
                            editHumProm.setText("0");
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
                editHumMin = findViewById(R.id.humMin);
                editHumMin.setText("0");
                editHumMax = findViewById(R.id.humMax);
                editHumMax.setText("0");
                editHumProm = findViewById(R.id.humProm);
                editHumProm.setText("0");
                //cuando la fecha ingresada es superior a la actual envio el siguiente mensaje
                Log.d("LOG WS", error.toString());
                generateToast("Error: No existen datos para la fecha seleccionada");
            }
        }
        );
        requestQueue.add(request);
    }

}
