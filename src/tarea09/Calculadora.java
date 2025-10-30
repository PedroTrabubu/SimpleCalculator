package tarea09;

// Librerías para JavaFX
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// Librerías exp4j
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Calculadora básica con soporte para teclado, operadores y paréntesis.
 * Autor: Pedro Blanquer
 */
public class Calculadora extends Application {

    private TextField pantallaCalcu;

    @Override
    public void start(Stage escenario) {
        // Pantalla
        pantallaCalcu = new TextField();
        pantallaCalcu.setEditable(false);
        pantallaCalcu.setPrefHeight(40);
        pantallaCalcu.setMaxWidth(320);
        pantallaCalcu.setPrefWidth(320);
        pantallaCalcu.getStyleClass().add("text-field");

        // Configuración principal
        escenario.setTitle("Calculadora de Pedro");
        escenario.setResizable(false);
        escenario.getIcons().add(new Image(getClass().getResource("logoCalcu.png").toString()));

        // Rejilla de botones
        GridPane rejillaBotones = new GridPane();
        rejillaBotones.setVgap(10);
        rejillaBotones.setHgap(10);
        rejillaBotones.setAlignment(Pos.CENTER);
        rejillaBotones.setPadding(new Insets(10));

        // Botones
        String[] botones = {
            "7", "8", "9", "/", "(",
            "4", "5", "6", "*", ")",
            "1", "2", "3", "-", ".",
            "0", "C", "<", "+", "="
        };

        int fila = 1, columna = 0;
        for (String texto : botones) {
            Button botonCalcu = new Button(texto);
            botonCalcu.setPrefSize(50, 60);
            botonCalcu.getStyleClass().add("button");

            if (texto.matches("[\\/()*\\-\\.\\+]")) botonCalcu.getStyleClass().add("operador");
            if (texto.equals("=")) botonCalcu.getStyleClass().add("igual");
            if (texto.equals("C") || texto.equals("<")) botonCalcu.getStyleClass().add("limpiar");

            rejillaBotones.add(botonCalcu, columna, fila);
            botonCalcu.setOnAction(e -> procesoDeEntrada(texto));

            columna++;
            if (columna > 4) { columna = 0; fila++; }
        }

        // Añadimos pantalla arriba
        rejillaBotones.add(pantallaCalcu, 0, 0, 5, 1);

        // Escena + CSS
        Scene scene = new Scene(rejillaBotones, 300, 400);
        scene.getStylesheets().add(getClass().getResource("calculadora.css").toExternalForm());

        // -----------------------------------
        // ENTRADA DESDE EL TECLADO (CORREGIDO)
        // -----------------------------------

        // Teclas especiales
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    procesoDeEntrada("=");

                    // Feedback visual del botón "="
                    rejillaBotones.getChildren().forEach(node -> {
                        if (node instanceof Button boton && boton.getText().equals("=")) {
                            String estiloOriginal = boton.getStyle();
                            boton.setStyle("-fx-background-color: #a0d2ff;");
                            PauseTransition pausa = new PauseTransition(Duration.millis(150));
                            pausa.setOnFinished(e -> boton.setStyle(estiloOriginal));
                            pausa.play();
                        }
                    });
                }
                case BACK_SPACE -> procesoDeEntrada("<");
                case DELETE -> procesoDeEntrada("C");
                default -> {}
            }
        });

        // Caracteres imprimibles (números y operadores)
        scene.setOnKeyTyped(event -> {
            String caracter = event.getCharacter();
            if (caracter == null || caracter.isEmpty()) return;

            if (caracter.matches("[0-9+\\-*/().]")) {
                procesoDeEntrada(caracter);

                // Feedback visual
                rejillaBotones.getChildren().forEach(node -> {
                    if (node instanceof Button boton && boton.getText().equals(caracter)) {
                        String estiloOriginal = boton.getStyle();
                        boton.setStyle("-fx-background-color: #a0d2ff;");
                        PauseTransition pausa = new PauseTransition(Duration.millis(120));
                        pausa.setOnFinished(e -> boton.setStyle(estiloOriginal));
                        pausa.play();
                    }
                });
            }
        });

        // Foco automático
        scene.setOnMouseClicked(e -> scene.getRoot().requestFocus());
        scene.getRoot().requestFocus();

        // Mostrar ventana
        escenario.setScene(scene);
        escenario.show();
    }

    /**
     * Procesa la entrada desde botones o teclado
     */
    public void procesoDeEntrada(String entrada) {
        String contenidoActual = pantallaCalcu.getText();

        switch (entrada) {
            case "C" -> pantallaCalcu.clear();

            case "<" -> {
                if (!contenidoActual.isEmpty()) {
                    pantallaCalcu.setText(contenidoActual.substring(0, contenidoActual.length() - 1));
                }
                if (contenidoActual.equals("Error de cálculo") || contenidoActual.equals("Expresión inválida")) {
                    pantallaCalcu.clear();
                }
            }

            case "." -> {
                int i = contenidoActual.length() - 1;
                boolean sePuedePonerPunto = true;
                while (i >= 0 && (Character.isDigit(contenidoActual.charAt(i)) || contenidoActual.charAt(i) == '.')) {
                    if (contenidoActual.charAt(i) == '.') sePuedePonerPunto = false;
                    i--;
                }
                if (sePuedePonerPunto) pantallaCalcu.appendText(".");
            }

            case "+", "-", "*", "/" -> {
                if (!contenidoActual.isEmpty()) {
                    char ultimo = contenidoActual.charAt(contenidoActual.length() - 1);
                    if (Character.isDigit(ultimo) || ultimo == ')') pantallaCalcu.appendText(entrada);
                }
            }

            case "(" -> {
                if (!contenidoActual.isEmpty()) {
                    char ultimo = contenidoActual.charAt(contenidoActual.length() - 1);
                    if (Character.isDigit(ultimo) || ultimo == ')') pantallaCalcu.appendText("*(");
                    else pantallaCalcu.appendText("(");
                } else pantallaCalcu.appendText("(");
            }

            case ")" -> {
                int abiertos = 0, cerrados = 0;
                for (char c : contenidoActual.toCharArray()) {
                    if (c == '(') abiertos++;
                    if (c == ')') cerrados++;
                }
                if (abiertos > cerrados) {
                    if (!contenidoActual.isEmpty()) {
                        char ultimo = contenidoActual.charAt(contenidoActual.length() - 1);
                        if (Character.isDigit(ultimo) || ultimo == ')') pantallaCalcu.appendText(")");
                    }
                }
            }

            case "=" -> {
                if (contenidoActual.matches("[0-9+\\-*/().]*")) {
                    try {
                        Expression expr = new ExpressionBuilder(contenidoActual).build();
                        double resultado = expr.evaluate();
                        pantallaCalcu.setText(contenidoActual + "=" + resultado);
                    } catch (Exception e) {
                        pantallaCalcu.setText("Error de cálculo");
                    }
                } else pantallaCalcu.setText("Expresión inválida");
            }

            default -> {
                if (!contenidoActual.isEmpty()) {
                    char ultimo = contenidoActual.charAt(contenidoActual.length() - 1);
                    if (ultimo == ')') pantallaCalcu.appendText("*" + entrada);
                    else pantallaCalcu.appendText(entrada);
                } else pantallaCalcu.appendText(entrada);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
