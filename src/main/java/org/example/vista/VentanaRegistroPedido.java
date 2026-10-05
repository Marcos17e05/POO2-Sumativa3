package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.EstadoPedido;
import org.example.modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaRegistroPedido extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JLabel lblId;
    private JLabel lblDireccion;
    private JLabel lblTipo;
    private JLabel lblEstado;
    private JTextField txtId;
    private JTextField txtDireccion;
    private JComboBox<String> cmbTipo;
    private JComboBox<EstadoPedido> cmbEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnFiltrar;
    private JButton btnRefrescar;
    private JComboBox<String> cmbFiltroTipo;
    private JComboBox<String> cmbFiltroEstado;
    private JTable tblPedidos;

    private final ControladorPedidos controlador;
    private DefaultTableModel modeloTabla;
    private Integer idSeleccionado;

    public VentanaRegistroPedido(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Gestión de Pedidos");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        inicializarCombos();
        inicializarTabla();
        inicializarEventos();
        limpiarFormulario();
        refrescarTabla(true);
    }

    private void inicializarCombos() {
        cmbTipo.setModel(new DefaultComboBoxModel<>(new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"}));
        cmbEstado.setModel(new DefaultComboBoxModel<>(EstadoPedido.values()));
        cmbFiltroTipo.setModel(new DefaultComboBoxModel<>(new String[]{"TODOS", "COMIDA", "ENCOMIENDA", "EXPRESS"}));
        cmbFiltroEstado.setModel(new DefaultComboBoxModel<>(new String[]{"TODOS", "PENDIENTE", "EN_REPARTO", "ENTREGADO"}));
    }

    private void inicializarTabla() {
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Dirección", "Tipo", "Estado", "Último repartidor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPedidos.setModel(modeloTabla);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void inicializarEventos() {
        btnGuardar.addActionListener(e -> guardarPedido());
        btnActualizar.addActionListener(e -> actualizarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnFiltrar.addActionListener(e -> refrescarTabla(true));
        btnRefrescar.addActionListener(e -> {
            cmbFiltroTipo.setSelectedIndex(0);
            cmbFiltroEstado.setSelectedIndex(0);
            refrescarTabla(true);
        });
        tblPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private void guardarPedido() {
        try {
            Pedido pedido = controlador.registrarPedido(txtDireccion.getText(), (String) cmbTipo.getSelectedItem(), (EstadoPedido) cmbEstado.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Pedido #" + pedido.getIdPedido() + " registrado correctamente.", "SpeedFast", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo registrar");
        }
    }

    private void actualizarPedido() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione un pedido de la tabla.", "Sin selección");
            return;
        }
        try {
            controlador.actualizarPedido(idSeleccionado, txtDireccion.getText(), (String) cmbTipo.getSelectedItem(), (EstadoPedido) cmbEstado.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.");
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo actualizar");
        }
    }

    private void eliminarPedido() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione un pedido de la tabla.", "Sin selección");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar el pedido #" + idSeleccionado + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminarPedido(idSeleccionado);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo eliminar");
        }
    }

    private void cargarSeleccion() {
        int fila = tblPedidos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtId.setText(String.valueOf(idSeleccionado));
        txtDireccion.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        cmbTipo.setSelectedItem(String.valueOf(modeloTabla.getValueAt(fila, 2)).toUpperCase());
        Object estado = modeloTabla.getValueAt(fila, 3);
        if (estado instanceof EstadoPedido) {
            cmbEstado.setSelectedItem(estado);
        }
        actualizarEstadoBotones();
    }

    private void refrescarTabla(boolean mostrarError) {
        String tipo = (String) cmbFiltroTipo.getSelectedItem();
        String estadoTexto = (String) cmbFiltroEstado.getSelectedItem();
        if ("TODOS".equals(tipo)) {
            tipo = null;
        }
        EstadoPedido estado = null;
        if (estadoTexto != null && !"TODOS".equals(estadoTexto)) {
            estado = EstadoPedido.valueOf(estadoTexto);
        }

        try {
            modeloTabla.setRowCount(0);
            for (Pedido pedido : controlador.getPedidos(tipo, estado)) {
                modeloTabla.addRow(new Object[]{pedido.getIdPedido(), pedido.getDireccionEntrega(), pedido.getTipoPedido(), pedido.getEstado(), pedido.getRepartidorAsignado()});
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarError(ex.getMessage(), "Error de base de datos");
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtId.setText("Automático");
        txtId.setEnabled(false);
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedItem(EstadoPedido.PENDIENTE);
        tblPedidos.clearSelection();
        txtDireccion.requestFocus();
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
