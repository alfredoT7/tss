import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RechazoLogica {

    public static final double C_EJ1 = 4.0 / 3.0;
    public static final double C_EJ2 = 4.0 / 7.0;
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");

    public static class FilaSimulacion {
        public final int iteracion;
        public final double r1, r2, x, fx, c, limite;
        public final String funcion;
        public final boolean aceptado;

        public FilaSimulacion(int iteracion, double r1, double r2,
                              double x, String funcion, double fx, double c,
                              double limite, boolean aceptado) {
            this.iteracion = iteracion;
            this.r1 = r1;
            this.r2 = r2;
            this.x = x;
            this.funcion = funcion;
            this.fx = fx;
            this.c = c;
            this.limite = limite;
            this.aceptado = aceptado;
        }
    }

    public static class PuntoGrafica {
        public final double x, r2, limite;
        public final boolean aceptado;

        public PuntoGrafica(double x, double r2, double limite, boolean aceptado) {
            this.x = x;
            this.r2 = r2;
            this.limite = limite;
            this.aceptado = aceptado;
        }
    }

    public static class ResultadoSimulacion {
        public final List<FilaSimulacion> filas;
        public final List<PuntoGrafica> puntos;
        public final int aceptados;
        public final int rechazados;

        public ResultadoSimulacion(List<FilaSimulacion> filas,
                                   List<PuntoGrafica> puntos,
                                   int aceptados, int rechazados) {
            this.filas = filas;
            this.puntos = puntos;
            this.aceptados = aceptados;
            this.rechazados = rechazados;
        }

        public int total() {
            return aceptados + rechazados;
        }

        public double porcentajeAceptacion() {
            return total() == 0 ? 0.0 : (aceptados * 100.0 / total());
        }
    }

    public static double calcularX(int ejercicio, double r1) {
        return ejercicio == 1 ? 4 + 2 * r1 : 1.5 * r1;
    }

    public static String determinarFuncion(int ejercicio, double x) {
        if (ejercicio == 1) return x <= 5.0 ? "F1" : "F2";
        return x <= 1.0 ? "F1" : "F2";
    }

    public static double calcularFx(int ejercicio, double x) {
        if (ejercicio == 1) return x <= 5.0 ? -0.5 * x + 2.75 : 0.5 * x - 2.25;
        return x <= 1.0 ? 0.25 : 3.0 * x - 2.75;
    }

    public static double calcularC(int ejercicio) {
        return ejercicio == 1 ? C_EJ1 : C_EJ2;
    }

    public static double calcularLimite(int ejercicio, double x) {
        return calcularFx(ejercicio, x) * calcularC(ejercicio);
    }

    public static FilaSimulacion resolverIteracion(int ejercicio, int iteracion,
                                                   double r1, double r2) {
        double x = calcularX(ejercicio, r1);
        String funcion = determinarFuncion(ejercicio, x);
        double fx = calcularFx(ejercicio, x);
        double c = calcularC(ejercicio);
        double limite = fx * c;
        boolean aceptado = r2 <= limite;

        return new FilaSimulacion(iteracion, r1, r2, x, funcion, fx, c, limite, aceptado);
    }

    public static ResultadoSimulacion simular(int ejercicio, int n, Random random) {
        List<FilaSimulacion> filas = new ArrayList<>();
        List<PuntoGrafica> puntos = new ArrayList<>();
        int aceptados = 0;
        int rechazados = 0;

        for (int i = 1; i <= n; i++) {
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();

            FilaSimulacion fila = resolverIteracion(ejercicio, i, r1, r2);
            filas.add(fila);
            puntos.add(new PuntoGrafica(fila.x, fila.r2, fila.limite, fila.aceptado));

            if (fila.aceptado) aceptados++; else rechazados++;
        }

        return new ResultadoSimulacion(filas, puntos, aceptados, rechazados);
    }

    public static ResultadoSimulacion simularConDatos(int ejercicio,
                                                      double[] r1, double[] r2) {
        List<FilaSimulacion> filas = new ArrayList<>();
        List<PuntoGrafica> puntos = new ArrayList<>();
        int aceptados = 0;
        int rechazados = 0;

        for (int i = 0; i < r1.length; i++) {
            FilaSimulacion fila = resolverIteracion(ejercicio, i + 1, r1[i], r2[i]);
            filas.add(fila);
            puntos.add(new PuntoGrafica(fila.x, fila.r2, fila.limite, fila.aceptado));

            if (fila.aceptado) aceptados++; else rechazados++;
        }

        return new ResultadoSimulacion(filas, puntos, aceptados, rechazados);
    }

    public static List<String> generarPasosTeoricos(int ejercicio) {
        List<String> pasos = new ArrayList<>();
        pasos.add(
            "PROCESO DEL METODO DEL RECHAZO\n\n"
            + "PARTE 1: PARAMETROS Y RESOLUCION TEORICA\n\n"
            + "Ejercicio seleccionado: Ejercicio " + ejercicio
        );

        pasos.add(
            "Paso 1. Generacion de numeros aleatorios\n"
            + "Se generan dos numeros aleatorios uniformes:\n"
            + "R1 en [0,1]\n"
            + "R2 en [0,1]\n"
            + "Estos valores seran generados computacionalmente mediante random.nextDouble() o Math.random()."
        );

        if (ejercicio == 1) {
            pasos.add(
                "Paso 2. Determinacion de la variable aleatoria x uniforme\n"
                + "Aqui se muestra la transformacion general de R1 hacia el intervalo del ejercicio.\n\n"
                + "Intervalo: 4 <= x <= 6\n"
                + "x = a + (b - a)R1\n"
                + "x = 4 + (6 - 4)R1\n"
                + "x = 4 + 2R1"
            );

            pasos.add(
                "Paso 3. Definicion de la funcion f(x(R1))\n"
                + "f(x) = -0.5x + 2.75, para 4 <= x <= 5\n"
                + "f(x) = 0.5x - 2.25, para 5 < x <= 6\n\n"
                + "Reemplazando x(R1):\n"
                + "Si 4 <= x <= 5:\n"
                + "f(x(R1)) = -0.5(4 + 2R1) + 2.75\n\n"
                + "Si 5 < x <= 6:\n"
                + "f(x(R1)) = 0.5(4 + 2R1) - 2.25"
            );

            pasos.add(
                "Paso 4. Determinacion de la desigualdad de aceptacion\n"
                + "R2 <= f(x) * C\n\n"
                + "fmax = 3/4 = 0.75\n"
                + "C = 1 / fmax\n"
                + "C = 1 / (3/4)\n"
                + "C = 4/3\n\n"
                + "Desigualdad:\n"
                + "R2 <= f(x) * 4/3"
            );
        } else {
            pasos.add(
                "Paso 2. Determinacion de la variable aleatoria x uniforme\n"
                + "Aqui se muestra la transformacion general de R1 hacia el intervalo del ejercicio.\n\n"
                + "Intervalo: 0 <= x <= 3/2\n"
                + "x = a + (b - a)R1\n"
                + "x = 0 + (3/2 - 0)R1\n"
                + "x = (3/2)R1"
            );

            pasos.add(
                "Paso 3. Definicion de la funcion f(x(R1))\n"
                + "f(x) = 0.25, para 0 <= x <= 1\n"
                + "f(x) = 3x - 2.75, para 1 < x <= 1.5\n\n"
                + "Reemplazando x(R1):\n"
                + "Si 0 <= x <= 1:\n"
                + "f(x(R1)) = 0.25\n\n"
                + "Si 1 < x <= 1.5:\n"
                + "f(x(R1)) = 3((3/2)R1) - 2.75"
            );

            pasos.add(
                "Paso 4. Determinacion de la desigualdad de aceptacion\n"
                + "R2 <= f(x) * C\n\n"
                + "fmax = 7/4 = 1.75\n"
                + "C = 1 / fmax\n"
                + "C = 1 / (7/4)\n"
                + "C = 4/7\n\n"
                + "Desigualdad:\n"
                + "R2 <= f(x) * 4/7"
            );
        }

        pasos.add(
            "Paso 5. Verificacion de la desigualdad\n"
            + "Si R2 <= f(x) * C, entonces el valor x se acepta.\n"
            + "Si R2 > f(x) * C, entonces el valor x se rechaza.\n\n"
            + "Interpretacion:\n"
            + "Los valores aceptados forman la muestra simulada de la distribucion objetivo. "
            + "Los valores rechazados fueron generados como candidatos, pero no entran a la muestra final."
        );

        return pasos;
    }

    public static List<String> generarPasosIteraciones(ResultadoSimulacion resultado, int ejercicio,
                                                       int limiteIteraciones) {
        List<String> pasos = new ArrayList<>();
        pasos.add("PARTE 2: DESARROLLO OPERATIVO DE LAS ITERACIONES");

        int total = Math.min(limiteIteraciones, resultado.filas.size());
        for (int i = 0; i < total; i++) {
            FilaSimulacion fila = resultado.filas.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("ITERACION ").append(fila.iteracion).append("\n\n");
            sb.append("Paso 1. Generar R1 y R2\n");
            sb.append("R1 = ").append(DF4.format(fila.r1)).append("\n");
            sb.append("R2 = ").append(DF4.format(fila.r2)).append("\n");
            sb.append("Los valores fueron generados usando random.nextDouble() o Math.random().\n\n");

            sb.append("Paso 2. Calcular x\n");
            sb.append(describirTransformacion(ejercicio, fila.r1, fila.x)).append("\n\n");

            sb.append("Paso 3. Evaluar f(x)\n");
            sb.append(describirTramo(ejercicio, fila.x, fila.funcion)).append("\n");
            sb.append(describirFx(ejercicio, fila)).append("\n\n");

            sb.append("Paso 4. Calcular limite de aceptacion\n");
            sb.append("C = ").append(describirC(ejercicio)).append("\n");
            sb.append("Limite = f(x) * C\n");
            sb.append("Limite = ").append(DF4.format(fila.fx)).append(" * ")
              .append(DF4.format(fila.c)).append(" = ").append(DF4.format(fila.limite)).append("\n\n");

            sb.append("Paso 5. Verificar desigualdad\n");
            sb.append("R2 <= limite\n");
            sb.append(DF4.format(fila.r2)).append(" <= ").append(DF4.format(fila.limite)).append("\n");
            sb.append("Resultado: ").append(fila.aceptado ? "Aceptado" : "Rechazado").append("\n");
            sb.append("Interpretacion: ").append(describirInterpretacion(fila.aceptado));
            pasos.add(sb.toString());
        }

        if (resultado.filas.size() > total) {
            pasos.add(
                "Solo se muestra el desarrollo de las primeras " + total + " iteraciones.\n"
                + "La tabla conserva todas las iteraciones generadas."
            );
        }

        return pasos;
    }

    private static String describirTransformacion(int ejercicio, double r1, double x) {
        if (ejercicio == 1) {
            return "x = 4 + 2(" + DF4.format(r1) + ") = " + DF4.format(x);
        }
        return "x = (3/2)(" + DF4.format(r1) + ") = " + DF4.format(x);
    }

    private static String describirTramo(int ejercicio, double x, String funcion) {
        if (ejercicio == 1) {
            if ("F1".equals(funcion)) {
                return "Como x = " + DF4.format(x) + " pertenece al tramo 4 <= x <= 5, se usa F1.";
            }
            return "Como x = " + DF4.format(x) + " pertenece al tramo 5 < x <= 6, se usa F2.";
        }

        if ("F1".equals(funcion)) {
            return "Como x = " + DF4.format(x) + " pertenece al tramo 0 <= x <= 1, se usa F1.";
        }
        return "Como x = " + DF4.format(x) + " pertenece al tramo 1 < x <= 1.5, se usa F2.";
    }

    private static String describirFx(int ejercicio, FilaSimulacion fila) {
        if (ejercicio == 1) {
            if ("F1".equals(fila.funcion)) {
                return "f(x) = -0.5(" + DF4.format(fila.x) + ") + 2.75 = " + DF4.format(fila.fx);
            }
            return "f(x) = 0.5(" + DF4.format(fila.x) + ") - 2.25 = " + DF4.format(fila.fx);
        }

        if ("F1".equals(fila.funcion)) {
            return "f(x) = 0.25";
        }
        return "f(x) = 3(" + DF4.format(fila.x) + ") - 2.75 = " + DF4.format(fila.fx);
    }

    private static String describirC(int ejercicio) {
        return ejercicio == 1 ? "4/3 = 1.3333" : "4/7 = 0.5714";
    }

    private static String describirInterpretacion(boolean aceptado) {
        if (aceptado) {
            return "el valor x entra a la muestra final porque R2 no supera el limite de aceptacion.";
        }
        return "el valor x fue generado, pero no entra a la muestra final porque R2 supera el limite de aceptacion.";
    }
}
