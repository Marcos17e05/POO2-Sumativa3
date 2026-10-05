package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaRegistroRepartidor extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JLabel lblNombre;
    private JTextField txtNombre;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnRefrescar;
    private JTable tblRepartidores;

    private final ControladorPedidos controlador;
    private DefaultTableModel modeloTabla;
    private Integer idSeleccionado;

    public VentanaRegistroRepartidor(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Gestión de Repartidores");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        inicializarTabla();
        inicializarEventos();
        refrescarTabla(true);
        actualizarEstadoBotones();
    }

    private void inicializarTabla() {
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRepartidores.setModel(modeloTabla);
        tblRepartidores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void inicializarEventos() {
        btnGuardar.addActionListener(e -> guardarRepartidor());
        btnActualizar.addActionListener(e -> actualizarRepartidor());
        btnEliminar.addActionListener(e -> eliminarRepartidor());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRefrescar.addActionListener(e -> refrescarTabla(true));

        tblRepartidores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private void guardarRepartidor() {
        try {
            Repartidor repartidor = controlador.registrarRepartidor(txtNombre.getText());
            JOptionPane.showMessageDialog(this, "Repartidor #" + repartidor.getId() + " registrado correctamente.", "SpeedFast", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo registrar");
        }
    }

    private void actualizarRepartidor() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione un repartidor de la tabla.", "Sin selección");
            return;
        }
        try {
            controlador.actualizarRepartidor(idSeleccionado, txtNombre.getText());
            JOptionPane.showMessageDialog(this, "Repartidor actualizado correctamente.");
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo actualizar");
        }
    }

    private void eliminarRepartidor() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione un repartidor de la tabla.", "Sin selección");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar el repartidor #" + idSeleccionado + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminarRepartidor(idSeleccionado);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo eliminar");
        }
    }

    private void cargarSeleccion() {
        int fila = tblRepartidores.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        actualizarEstadoBotones();
    }

    private void refrescarTabla(boolean mostrarError) {
        try {
            modeloTabla.setRowCount(0);
            for (Repartidor repartidor : controlador.getRepartidores()) {
                modeloTabla.addRow(new Object[]{repartidor.getId(), repartidor.getNombre()});
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarError(ex.getMessage(), "Error de base de datos");
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtNombre.setText("");
        tblRepartidores.clearSelection();
        txtNombre.requestFocus();
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        boolean seleccionado = idSeleccionado != null;
        btnActualizar.setEnabled(seleccionado);
        btnEliminar.setEnabled(seleccionado);
    }

    private void mostrarError(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }
}
