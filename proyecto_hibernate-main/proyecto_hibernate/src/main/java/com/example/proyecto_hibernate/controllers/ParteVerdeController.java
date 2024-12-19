package com.example.proyecto_hibernate.controllers;

import com.example.proyecto_hibernate.CRUD.*;
import com.example.proyecto_hibernate.classes.*;
import com.example.proyecto_hibernate.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class ParteVerdeController implements Initializable, Configurable {

    @FXML
    private Button bt_actualizar;

    @FXML
    private Button bt_exportar;

    @FXML
    private Button bt_crear;

    @FXML
    private Button bt_parteNaranja;

    @FXML
    private Button bt_parteRojo;

    @FXML
    private Button bt_parteVerde;

    @FXML
    private ComboBox<String> cb_horaParte;

    @FXML
    private DatePicker dp_fechaParte;

    @FXML
    private TextArea txt_sancion;

    @FXML
    private Label grupo_alumno;

    @FXML
    private Label nombre_profesor;

    @FXML
    private TextArea txt_descripcion;

    @FXML
    private TextField txt_expedienteAlumno;

    private PartesCRUD parteCRUD = new PartesCRUD();

    private AlumnosCRUD alumnoCRUD = new AlumnosCRUD();

    private Alumnos alumno;

    private Boolean reset = false;

    private ListaPartesController listaPartesController = GuardarController.getController();

    @FXML
    void onActualizarClick(ActionEvent event) {
        PartesIncidencia parte = GuardarParte.getParte();
        parte.setFecha(dp_fechaParte.getValue());
        parte.setHora(cb_horaParte.getValue());
        parte.setDescripcion(txt_descripcion.getText());
        parte.setSancion(txt_sancion.getText());
        parte.setColor(ColorParte.VERDE);
        parte.setPuntos_parte(parte.getColor().getPuntos());

        alumno = parte.getAlumno();
        alumnoCRUD.actualizarPuntosAlumno(alumno, parte, false); //actualizar los puntos, al modificar el parte
        if(parteCRUD.actualizarParte(parte)){
            Alerta.mensajeInfo("ÉXITO", null, "Parte actualizado correctamente.");
            // Cerrar la ventana actual
            Stage stage = (Stage) bt_actualizar.getScene().getWindow();
            stage.close();

            // Notificar a la lista de partes para que se recargue
            listaPartesController.recargarListaPartes();
        } else {
            Alerta.mensajeError(null, "No se pudo actualizar el parte.");
        }
    }


    @FXML
    void onCrearClick(ActionEvent event) {
        if (txt_expedienteAlumno.getText().isEmpty() || dp_fechaParte.getValue() == null || txt_descripcion.getText().isEmpty() || cb_horaParte.getValue().isEmpty() || txt_sancion.getText().isEmpty()){
            Alerta.mensajeError("Campos vacíos", "Por favor, completa todos los campos.");
        } else { //si todos los campos están correctos -> creo el parte y lo introduzco en la BD
            PartesIncidencia parte = new PartesIncidencia(alumno, GuardarProfesor.getProfesor(), alumno.getGrupo(), dp_fechaParte.getValue(), cb_horaParte.getValue(), txt_descripcion.getText(), txt_sancion.getText(), ColorParte.VERDE);
            alumnoCRUD.actualizarPuntosAlumno(alumno, parte, true);
            if(parteCRUD.crearParte(parte)){
                Alerta.mensajeInfo("ÉXITO", "Parte creado", "El parte ha sido creado correctamente.");
            } else {
                Alerta.mensajeError("Error al crear el parte", "El parte duplicado.");
            }
            limpiarCampos();
        }
    }


    @FXML
    void onExpedienteAlumnoChange(KeyEvent event) {
        String numExpediente = txt_expedienteAlumno.getText();

        if (!numExpediente.isEmpty()) {
            // Buscar el alumno con el número de expediente
            alumno = alumnoCRUD.buscarAlumnoPorExpediente(numExpediente);

            if (alumno != null) {
                // Si el alumno existe, mostrar el grupo en el Label
                grupo_alumno.setText(alumno.getGrupo().getNombreGrupo());
            } else {
                // Si no se encuentra el alumno, mostrar un mensaje de error
                grupo_alumno.setText("Alumno no encontrado.");
            }
        } else {
            // Si el campo está vacío, limpiar el Label
            grupo_alumno.setText("");
        }
    }


    @FXML
    void onParteNaranjaClick(ActionEvent event) {
        resetParte(reset);
        CambioEscena.cambiarEscena(bt_parteNaranja, "parte-naranja.fxml");
    }//onParteNaranjaClick

    @FXML
    void onParteRojoClick(ActionEvent event) {
        resetParte(reset);
        CambioEscena.cambiarEscena(bt_parteRojo, "parte-rojo.fxml");
    }//onParteRojoClick


    @FXML
    void onExportarClick(ActionEvent event) {
        if (GuardarParte.getParte() == null) {
            Alerta.mensajeError("Error", "No hay ningún parte cargado para exportar.");
            return;
        }

        PDPageContentStream contentStream = null;
        PDDocument document = null;
        try {
            // Crear documento PDF
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Iniciar flujo de contenido
            contentStream = new PDPageContentStream(document, page);

            // Dibujar el fondo verde
            contentStream.setNonStrokingColor(Color.decode(ColorParte.VERDE.getCodigo_color())); // RGB para verde
            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            contentStream.fill();

            contentStream.setNonStrokingColor(0, 0, 0); // RGB para negro

            // Añadir la imagen (logo) ajustada más arriba y a la derecha
            try {
                InputStream logoStream = getClass().getResourceAsStream("/img/logo.png");
                if (logoStream == null) {
                    Alerta.mensajeError("Error", "No se encontró el logo en '/img/logo.png'.");
                    return;
                }
                PDImageXObject logo = PDImageXObject.createFromByteArray(
                        document,
                        logoStream.readAllBytes(),
                        "logo.png"
                );
                contentStream.drawImage(logo, 500, 650, 100, 100); // Posición ajustada
            } catch (IOException e) {
                e.printStackTrace();
                Alerta.mensajeError("Error", "Error al cargar la imagen del logo: " + e.getMessage());
            }


            // Escribir el título h1
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText("Parte de Incidencia");
            contentStream.endText();

            // Escribir el subtítulo h2
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 620);
            contentStream.showText("Detalles del Parte");
            contentStream.endText();

            // Escribir los detalles del parte
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            PartesIncidencia parte = GuardarParte.getParte();
            String content = String.format(
                    "Profesor: %s\n" +
                            "Alumno: %s\n" +
                            "Grupo: %s\n" +
                            "Fecha: %s\n" +
                            "Hora: %s\n\n" +
                            "Descripción:\n%s\n\n" +
                            "Sanción:\n%s\n",
                    parte.getProfesor().getNombre(),
                    parte.getAlumno().getNombre_alum(),
                    parte.getGrupo().getNombreGrupo(),
                    parte.getFecha(),
                    parte.getHora(),
                    parte.getDescripcion(),
                    parte.getSancion()
            );

            // Gestión de saltos de línea manualmente
            String[] lines = content.split("\n");
            float yPosition = 580; // Posición inicial en el eje Y
            for (String line : lines) {
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= 14; // Ajusta la posición para la siguiente línea
            }

            // Cerrar el flujo de contenido
            contentStream.close();

            // Usar FileChooser para seleccionar la ubicación de guardado
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            // Establecer el directorio predeterminado en las "Descargas" del usuario
            String userHome = System.getProperty("user.home");
            String downloadsFolder = userHome + "/Downloads";
            File defaultDirectory = new File(downloadsFolder);
            fileChooser.setInitialDirectory(defaultDirectory);

            // Abrir el diálogo de selección de archivo
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                String filePath = selectedFile.getAbsolutePath();
                document.save(filePath);
                document.close();

                // Notificar éxito
                Alerta.mensajeInfo("Éxito", "Exportación completada", "El parte se ha exportado como PDF:\n" + filePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alerta.mensajeError("Error", "Ocurrió un error al exportar el PDF.");
        } finally {
            try {
                if (contentStream != null) contentStream.close();
                if (document != null) document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cb_horaParte.getItems().addAll(
                "08:30-09:20",
                "09:25-10:15",
                "10:20-11:10",
                "11:40-12:30",
                "12:35-13:25",
                "13:30-14:20",
                "16:00-16:50",
                "16:55-17:45",
                "17:50-18:40",
                "18:55-19:45",
                "19:50-20:40",
                "20:45-21:35"
        );

        nombre_profesor.setText(GuardarProfesor.getProfesor().getNombre());

        if (GuardarParte.getParte() != null) {
            // Si ya hay un parte cargado
            txt_expedienteAlumno.setText(GuardarParte.getParte().getAlumno().getNumero_expediente());
            grupo_alumno.setText(GuardarParte.getParte().getGrupo().getNombreGrupo());
            dp_fechaParte.setValue(GuardarParte.getParte().getFecha());
            cb_horaParte.setValue(GuardarParte.getParte().getHora());
            txt_descripcion.setText(GuardarParte.getParte().getDescripcion());
            txt_sancion.setText(GuardarParte.getParte().getSancion());

            // Si se ha cargado un parte, habilitar el botón de actualización
            bt_actualizar.setDisable(false);
            bt_exportar.setDisable(false);
            bt_crear.setDisable(true); // Deshabilitar el botón de crear si ya existe un parte
        } else {
            // Si no se ha cargado un parte, mantener el botón de actualizar deshabilitado
            bt_actualizar.setDisable(true);
            bt_exportar.setDisable(true);
            bt_crear.setDisable(false); // Permitir crear un nuevo parte
        }
    }


    private void limpiarCampos() {
        txt_expedienteAlumno.clear();
        grupo_alumno.setText("");
        dp_fechaParte.setValue(null);
        cb_horaParte.setValue(null);
        txt_descripcion.clear();
        txt_sancion.setText("");
    }


    @Override
    public void configurarBotones(Boolean estado) {// Deshabilita o habilita el botón según el estado.
        bt_parteNaranja.setDisable(estado); //true -> deshabilitado
        bt_parteRojo.setDisable(estado);
        bt_crear.setDisable(!estado);
        bt_actualizar.setDisable(estado);
        txt_expedienteAlumno.setEditable(estado); //para que no se pueda editar el alumno
        reset = estado;
    }


    private void resetParte(Boolean reset) {
        if(reset){
            GuardarParte.resetParte();
        }
    }
}//class