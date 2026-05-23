import java.util.Map;

public class Ronda {
    public static  int LANZAMIENTO = 1;
    public Ronda(){
        LANZAMIENTO = 1;
    }
    public static Map<String,Integer> simularRonda(int capitalActual, Map<String, Integer> respuesta){
        boolean resultadoPrimerLanzamiento = primerLanzamiento(respuesta);
        respuesta.put("gano", resultadoPrimerLanzamiento ? 1 : 0);
        respuesta.put("lanzamientos", LANZAMIENTO);
        return respuesta;
    }
    private static boolean primerLanzamiento(Map<String, Integer> respuesta){
        int dado1 = LanzamientoDatos.lanzarDado();
        int dado2 = LanzamientoDatos.lanzarDado();
        int sumaDados = dado1 + dado2;
        respuesta.put("Lanzamiento Ronda " + LANZAMIENTO, sumaDados);
        if(sumaDados == 7 || sumaDados == 11){
            return true;
        }else{
            if(sumaDados == 2 || sumaDados==3 || sumaDados==12){
                return false;
            }else{
                return otrosLanzamientos(respuesta);
            }
        }
    }
    private static boolean otrosLanzamientos(Map<String, Integer> respuesta){
        int dado1 = LanzamientoDatos.lanzarDado();
        int dado2 = LanzamientoDatos.lanzarDado();
        int sumaDados = dado1 + dado2;
        LANZAMIENTO++;
        respuesta.put("Lanzamiento Ronda " + LANZAMIENTO, sumaDados);
        Integer objetivo = respuesta.get("Lanzamiento Ronda 1");
        if (objetivo != null && sumaDados == objetivo.intValue()) {
            return true;
        }
        if (sumaDados == 7) {
            return false;
        }
        return otrosLanzamientos(respuesta);
    }
}
