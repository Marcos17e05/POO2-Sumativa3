package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Entrega;
import org.example.modelo.EstadoPedido;
import org.example.modelo.Pedido;
import org.example.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class VentanaAsignacionEntrega extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JLabel lblId;
    private JLabel lblPedido;
    private JLabel lblRepartidor;
    private JLabel lblFecha;
    private JLabel lblHora;
    private JTextField txtId;
    private JComboBox<Pedido> cmbPedido;
    private JComboBox<Repartidor> cmbRepartidor;
    private JTextField txtFecha;
    private JTextField txtHora;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnFiltrar;
    private JButton btnRefrescar;
    private JComboBox<Object> cmbFiltroPedido;
    private JComboBox<Object> cmbFiltroRepartidor;
    private JTable tblEntregas;

    private final ControladorPedidos controlador;
    private DefaultTableModel modeloTabla;
    private Integer idSeleccionado;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public VentanaAsignacionEntrega(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Gestión de Entregas");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(980, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        inicializarTabla();
        inicializarEventos();
        cargarCombos(true);
        limpiarFormulario();
        refrescarTabla(true);
    }

    private void inicializarTabla() {
        modeloTabla = new DefaultTableModel(new String[]{"ID", "ID Pedido", "Dirección", "ID Repartidor", "Repartidor", "Fecha", "Hora"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEntregas.setModel(modeloTabla);
        tblEntregas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void inicializarEventos() {
        btnGuardar.addActionListener(e -> guardarEntrega());
        btnActualizar.addActionListener(e -> actualizarEntrega());
        btnEliminar.addActionListener(e -> eliminarEntrega());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnFiltrar.addActionListener(e -> refrescarTabla(true));
        btnRefrescar.addActionListener(e -> {
            cargarCombos(true);
            refrescarTabla(true);
        });
        tblEntregas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private void cargarCombos(boolean mostrarError) {
        try {
            Integer pedidoSeleccionado = obtenerIdPedidoSeleccionado(cmbPedido.getSelectedItem());
            Integer repartidorSeleccionado = obtenerIdRepartidorSeleccionado(cmbRepartidor.getSelectedItem());
            Integer filtroPedido = obtenerIdPedidoSeleccionado(cmbFiltroPedido.getSelectedItem());
            Integer filtroRepartidor = obtenerIdRepartidorSeleccionado(cmbFiltroRepartidor.getSelectedItem());

            cmbPedido.removeAllItems();
            cmbRepartidor.removeAllItems();
            cmbFiltroPedido.removeAllItems();
            cmbFiltroRepartidor.removeAllItems();

            cmbFiltroPedido.addItem("TODOS");
            cmbFiltroRepartidor.addItem("TODOS");

            for (Pedido pedido : controlador.getPedidos()) {
                cmbPedido.addItem(pedido);
                cmbFiltroPedido.addItem(pedido);
            }
            for (Repartidor repartidor : controlador.getRepartidores()) {
                cmbRepartidor.addItem(repartidor);
                cmbFiltroRepartidor.addItem(repartidor);
            }

            seleccionarPedido(cmbPedido, pedidoSeleccionado);
            seleccionarRepartidor(cmbRepartidor, repartidorSeleccionado);
            seleccionarPedidoObjeto(cmbFiltroPedido, filtroPedido);
            seleccionarRepartidorObjeto(cmbFiltroRepartidor, filtroRepartidor);
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarError(ex.getMessage(), "Error de base de datos");
            }
        }
    }

    private void guardarEntrega() {
        try {
            Pedido pedido = (Pedido) cmbPedido.getSelectedItem();
            Repartidor repartidor = (Repartidor) cmbRepartidor.getSelectedItem();
            if (pedido == null || repartidor == null) {
                throw new IllegalArgumentException("Debe existir y seleccionar un pedido y un repartidor.");
            }

            if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
                throw new IllegalStateException("Solo se puede agendar e iniciar una entrega para un pedido PENDIENTE.");
            }

            Entrega entrega = controlador.registrarEntrega(pedido.getIdPedido(), repartidor.getId(), leerFecha(), leerHora());

            // Al agendar la entrega se inicia inmediatamente el Runnable del repartidor.
            // El controlador cambia el pedido a EN_REPARTO y el hilo lo deja ENTREGADO
            controlador.iniciarSimulacionEntrega(pedido.getIdPedido(), repartidor);

            JOptionPane.showMessageDialog(this, "Entrega #" + entrega.getId() + " registrada correctamente.\n" + "El pedido #" + pedido.getIdPedido() + " cambió a EN_REPARTO y la simulación ya comenzó.", "SpeedFast", JOptionPane.INFORMATION_MESSAGE);

            cargarCombos(false);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo registrar");
        }
    }

    private void actualizarEntrega() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione una entrega de la tabla.", "Sin selección");
            return;
        }

        try {
            Pedido pedido = (Pedido) cmbPedido.getSelectedItem();
            Repartidor repartidor = (Repartidor) cmbRepartidor.getSelectedItem();
            if (pedido == null || repartidor == null) {
                throw new IllegalArgumentException("Debe seleccionar un pedido y un repartidor.");
            }

            controlador.actualizarEntrega(idSeleccionado, pedido.getIdPedido(), repartidor.getId(), leerFecha(), leerHora());

            JOptionPane.showMessageDialog(this, "Entrega actualizada correctamente.");
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo actualizar");
        }
    }

    private void eliminarEntrega() {
        if (idSeleccionado == null) {
            mostrarError("Seleccione una entrega de la tabla.", "Sin selección");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar la entrega #" + idSeleccionado + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminarEntrega(idSeleccionado);
            limpiarFormulario();
            refrescarTabla(true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError(ex.getMessage(), "No se pudo eliminar");
        }
    }

    private void cargarSeleccion() {
        int fila = tblEntregas.getSelectedRow();
        if (fila < 0) {
            return;
        }

        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtId.setText(String.valueOf(idSeleccionado));
        int idPedido = (Integer) modeloTabla.getValueAt(fila, 1);
        int idRepartidor = (Integer) modeloTabla.getValueAt(fila, 3);
        txtFecha.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        txtHora.setText(String.valueOf(modeloTabla.getValueAt(fila, 6)));
        seleccionarPedido(cmbPedido, idPedido);
        seleccionarRepartidor(cmbRepartidor, idRepartidor);
        actualizarEstadoBotones();
    }

    private void refrescarTabla(boolean mostrarError) {
        Integer idPedido = obtenerIdPedidoSeleccionado(cmbFiltroPedido.getSelectedItem());
        Integer idRepartidor = obtenerIdRepartidorSeleccionado(cmbFiltroRepartidor.getSelectedItem());

        try {
            modeloTabla.setRowCount(0);
            for (Entrega entrega : controlador.getEntregas(idPedido, idRepartidor)) {
                modeloTabla.addRow(new Object[]{entrega.getId(), entrega.getIdPedido(), entrega.getDireccionPedido(), entrega.getIdRepartidor(), entrega.getNombreRepartidor(), entrega.getFecha() == null ? "" : entrega.getFecha(), entrega.getHora() == null ? "" : entrega.getHora().format(FORMATO_HORA)});
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarError(ex.getMessage(), "Error de base de datos");
            }
        }
    }

    private LocalDate leerFecha() {
        try {
            return LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha debe usar el formato AAAA-MM-DD. Ejemplo: 2026-10-05.");
        }
    }

    private LocalTime leerHora() {
        String texto = txtHora.getText().trim();
        try {
            if (texto.length() == 5) {
                texto += ":00";
            }
            return LocalTime.parse(texto, FORMATO_HORA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La hora debe usar HH:mm o HH:mm:ss. Ejemplo: 16:30:00.");
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtId.setText("Automático");
        txtId.setEnabled(false);
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().withNano(0).format(FORMATO_HORA));
        if (cmbPedido.getItemCount() > 0) {
            cmbPedido.setSelectedIndex(0);
        }
        if (cmbRepartidor.getItemCount() > 0) {
            cmbRepartidor.setSelectedIndex(0);
        }
        tblEntregas.clearSelection();
        actualizarEstadoBotones();
    }

    private Integer obtenerIdPedidoSeleccionado(Object item) {
        return item instanceof Pedido ? ((Pedido) item).getIdPedido() : null;
    }

    private Integer obtenerIdRepartidorSeleccionado(Object item) {
        return item instanceof Repartidor ? ((Repartidor) item).getId() : null;
    }

    private void seleccionarPedido(JComboBox<Pedido> combo, Integer id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getIdPedido() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarRepartidor(JComboBox<Repartidor> combo, Integer id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarPedidoObjeto(JComboBox<Object> combo, Integer id) {
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof Pedido && ((Pedido) item).getIdPedido() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarRepartidorObjeto(JComboBox<Object> combo, Integer id) {
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof Repartidor && ((Repartidor) item).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
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
