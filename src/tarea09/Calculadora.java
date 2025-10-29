package tarea09;

//Librerías para poder utilizar JavaFX
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//Librerías específicas para evaluar expresiones exp4j
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * La típica calculadora básica para realizar cálculos artitméticos como la suma, resta, multiplicación y división, en la que se permite el cálculo de expresiones combinadas.
 *
 * @author Pedro Blanquer
 */
public class Calculadora extends Application {
    //----------------------------------------------
    //          Declaración de variables 
    //----------------------------------------------

    private TextField pantallaCalcu;

    //----------------------------------------------
    //          Declaración de variables auxiliares 
    //----------------------------------------------  

    /*El método start es el punto de entrada para una aplicación JavaFX.
    Su función principal es inicializar y mostrar la interfaz 
    gráfica de usuario (GUI) de la aplicación. Se crea un diseño (layout), 
    se añaden controles (botones, etiquetas, campos, ...) y se crea la escena
    con un estilo, y se muestra el escenario.*/
    public void start(Stage escenario) {
        // Creamos la panatalla de la calculadora
        pantallaCalcu = new TextField();
        //Marcamos que no puede ser editada
        pantallaCalcu.setEditable(false);
        //Para no verlo muy baja la pantalla vamos a añadirle mas altura
        pantallaCalcu.setPrefHeight(40);
        //Ponemos el ajuste del ancho de la pantalla
        pantallaCalcu.setMaxWidth(320);
        pantallaCalcu.setPrefWidth(320);
        //Añadimos el CSS
        pantallaCalcu.getStyleClass().add("text-field");

        // Configuramos la ventana principal (escenario)
        escenario.setTitle("Calculadora de Pedro");
        escenario.setResizable(false); // La ventana no se puede redimensionar
        escenario.getIcons().add(new Image(getClass().getResource("logoCalcu.png").toString())); // Icono

        // Creamos la rejilla donde irán los botones
        GridPane rejillaBotones = new GridPane();
        rejillaBotones.setVgap(10); // Separación vertical entre botones
        rejillaBotones.setHgap(10); // Separación horizontal entre botones
        rejillaBotones.setAlignment(Pos.CENTER); // Centramos toda la rejilla
        rejillaBotones.setPadding(new Insets(10)); // Añadimos espacio alrededor de la rejilla

        // Array con todos los textos de los botones
        String[] botones = {
            "7", "8", "9", "/", "(",
            "4", "5", "6", "*", ")",
            "1", "2", "3", "-", ".",
            "0", "C", "<", "+", "="
        };

        // Variables para la posición de los botones en la rejilla (empezamos en la fila 1 porque la fila 0 es la pantalla)
        int fila = 1;
        int columna = 0;

        //Recorremos cada texto del array por cada botón
        for (String texto : botones) {
            // Creamos el botón y le asignamos el texto correspondiente
            Button botonCalcu = new Button(texto);
            // Ajustamos el tamaño del botón
            botonCalcu.setPrefSize(50, 60);
            // Le aplicamos la clase general para los botones
            botonCalcu.getStyleClass().add("button");

            // Si es un operador, le añadimos una clase extra para los estilos de operadores
            if (texto.matches("[\\/()*\\-\\.\\+]")) {
                botonCalcu.getStyleClass().add("operador");
            }
            // Si es el botón de igual, le damos su estilo especial
            if (texto.equals("=")) {
                botonCalcu.getStyleClass().add("igual");
            }
            // Si es un botón de limpiar (C o <), le damos otro estilo
            if (texto.equals("C") || texto.equals("<")) {
                botonCalcu.getStyleClass().add("limpiar");
            }
            // Añadimos el boton a su GridPane
            rejillaBotones.add(botonCalcu, columna, fila);

            // Asignamos la funcionalidad al boton
            botonCalcu.setOnAction(e -> procesoDeEntrada(texto));

            //Vamos comprobando la posicion para  ir colocando los botones en las filas y en las columnas
            columna++;
            if (columna > 4) {
                columna = 0;
                fila++;
            }
        }
        // Añadimos la pantalla de la calculadora en la parte superior de la rejilla (ocupa 5 columnas)
        rejillaBotones.add(pantallaCalcu, 0, 0, 5, 1);

        // Creamos la escena principal con la rejilla y aplicamos el CSS
        Scene scene = new Scene(rejillaBotones, 300, 400);
        scene.getStylesheets().add(getClass().getResource("calculadora.css").toExternalForm());
        
        // Mostramos la ventana
        escenario.setScene(scene);
        escenario.show();
    }

    /**
     * El método procesoDeEntrada maneja la entrada de datos en una calculadora. Toma una cadena de texto (entrada) y realiza diferentes acciones según el contenido de esa cadena, agregando al campo de texto, estableciendo el estado, controlando la adición de puntos decimales para evitar múltiples decimales en un número o evaluando la expresión mátemática para mostrar el resultado haciendo uso de la librería Exp4J.
     *
     * @param entrada Texto recogido de los diferentes textoBotones de la calculadora.
     */
    public void procesoDeEntrada(String entrada) {
    // Variable para almacenar el contenido actual de la pantalla
    String contenidoActual = pantallaCalcu.getText();

    // Iremos comprobando mediante el switch la entrada de datos
    switch (entrada) {
        // Caso para borrar todo: Limpia la pantalla
        case "C":
            pantallaCalcu.clear();
            break;

        // Caso para borrar el último carácter: Elimina el último carácter de la pantalla
        case "<":
            // Si la pantalla tiene texto, borramos el último carácter
            if (!contenidoActual.isEmpty()) {
                pantallaCalcu.setText(contenidoActual.substring(0, contenidoActual.length() - 1));
            }
            // Si el texto es un mensaje de error, lo eliminamos todo
            if (contenidoActual.equals("Error de cálculo") || contenidoActual.equals("Expresión inválida")) {
                pantallaCalcu.clear();
            }
            break;

        // Caso para insertar el punto decimal: Solo lo añadimos si no está repetido
        case ".":
            int i = contenidoActual.length() - 1;
            boolean sePuedePonerPunto = true;
            // Comprobamos los caracteres previos a ver si ya existe un punto
            while (i >= 0 && (Character.isDigit(contenidoActual.charAt(i)) || contenidoActual.charAt(i) == '.')) {
                // Si ya hay un punto, no permitimos agregar otro
                if (contenidoActual.charAt(i) == '.') {
                    sePuedePonerPunto = false;
                }
                i--;
            }
            // Si no hay punto, lo añadimos
            if (sePuedePonerPunto) {
                pantallaCalcu.appendText(".");
            }
            break;

        // Casos para los operadores: Solo los añadimos si el último carácter es un número o un paréntesis de cierre
        case "+":
        case "-":
        case "*":
        case "/":
            if (!contenidoActual.isEmpty()) {
                char ultimoCaracter = contenidoActual.charAt(contenidoActual.length() - 1);
                // Solo añadimos el operador si el último carácter es un número o un paréntesis de cierre
                if (Character.isDigit(ultimoCaracter) || ultimoCaracter == ')') {
                    pantallaCalcu.appendText(entrada);
                }
            }
            break;

        // Caso para el paréntesis de apertura: Añadimos el paréntesis de apertura si es necesario
        case "(":
            if (!contenidoActual.isEmpty()) {
                char ultimoCaracter = contenidoActual.charAt(contenidoActual.length() - 1);
                // Si el último carácter es un número o un paréntesis de cierre, precedemos el paréntesis de apertura con un '*'
                if (Character.isDigit(ultimoCaracter) || ultimoCaracter == ')') {
                    pantallaCalcu.appendText("*(");
                } else {
                    pantallaCalcu.appendText("(");
                }
            } else {
                // Si la pantalla está vacía, añadimos directamente el paréntesis de apertura
                pantallaCalcu.appendText("(");
            }
            break;

        // Caso para el paréntesis de cierre: Cerramos el paréntesis si hay un paréntesis de apertura correspondiente
        case ")":
            int parentesisAbiertos = 0,
             parentesisCerrados = 0;
            // Contamos cuántos paréntesis de apertura y cierre hay en la pantalla
            for (char caracter : contenidoActual.toCharArray()) {
                if (caracter == '(') {
                    parentesisAbiertos++;
                }
                if (caracter == ')') {
                    parentesisCerrados++;
                }
            }
            // Si hay más paréntesis abiertos que cerrados, podemos añadir un paréntesis de cierre
            if (parentesisAbiertos > parentesisCerrados) {
                if (!contenidoActual.isEmpty()) {
                    char ultimoCaracter = contenidoActual.charAt(contenidoActual.length() - 1);
                    // Solo añadimos el paréntesis de cierre si el último carácter es un número o un paréntesis de cierre
                    if (Character.isDigit(ultimoCaracter) || ultimoCaracter == ')') {
                        pantallaCalcu.appendText(")");
                    }
                }
            }
            break;

        // Caso para realizar el cálculo: Evaluamos la expresión matemática
        case "=":
            // Verificamos que la entrada solo contenga números y operadores válidos
            if (contenidoActual.matches("[0-9+\\-*/().]*")) {
                try {
                    // Intentamos evaluar la expresión matemática usando la librería ExpressionBuilder
                    Expression expresion = new ExpressionBuilder(contenidoActual).build();
                    double resultado = expresion.evaluate();
                    // Mostramos el resultado junto con la expresión
                    pantallaCalcu.setText(contenidoActual + "=" + resultado);
                } catch (Exception e) {
                    // Si hay un error al evaluar la expresión, mostramos un mensaje de error
                    pantallaCalcu.setText("Error de cálculo");
                }
            } else {
                // Si la expresión no es válida, mostramos un mensaje de expresión inválida
                pantallaCalcu.setText("Expresión inválida");
            }
            break;

        // Caso por defecto: Si es un número, lo añadimos al contenido
        default:
            if (!contenidoActual.isEmpty()) {
                char ultimoCaracter = contenidoActual.charAt(contenidoActual.length() - 1);
                // Si el último carácter es un paréntesis de cierre, añadimos un multiplicador antes de añadir el número
                if (ultimoCaracter == ')') {
                    pantallaCalcu.appendText("*" + entrada);
                } else {
                    pantallaCalcu.appendText(entrada);
                }
            } else {
                // Si la pantalla está vacía, simplemente añadimos el número
                pantallaCalcu.appendText(entrada);
            }
            break;
    }
}


    /**
     * Programa principal, lanza la aplicación.
     *
     * @param args Argumentos o parámetros (no hay en este caso)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
