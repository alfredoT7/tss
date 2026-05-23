import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        imprimirTerminalResultadoDeUnaRonda();
        
    }
    private static void imprimirTerminalResultadoDeUnaRonda(){
        System.out.println("Perdio o Gano");
        Map<String, Integer> respuesta = new HashMap<>();
        int capitalActual = 25;
        Map<String, Integer> resultado =  Ronda.simularRonda(capitalActual, respuesta);
        System.out.println("Gano? " + (resultado.get("gano") != null && resultado.get("gano") == 1));
        int lanzamientos = resultado.get("lanzamientos") != null ? resultado.get("lanzamientos") : 0;
        System.out.println("Lanzamientos: " + lanzamientos);
        for (int i = 1; i <= lanzamientos; i++) {
            Integer suma = respuesta.get("Lanzamiento Ronda " + i);
            System.out.println("Lanzamiento Ronda " + i + ": " + (suma != null ? suma : "(sin datos)"));
        }
    }
    
}