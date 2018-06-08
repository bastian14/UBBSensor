package cl.ubiobio.ubbsensor;

public class Datos {
    private String fecha;
    private String hora;
    private String valor;

    public Datos() {
    }

    public Datos(String fecha, String hora, String valor) {
        this.fecha = fecha;
        this.hora = hora;
        this.valor = valor;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
