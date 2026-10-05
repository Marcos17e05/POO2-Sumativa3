package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Entrega;
import org.example.modelo.Pedido;
import org.example.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;

public class VentanaListaPedidos extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JLabel lblMostrar;
    private JComboBox<String> cmbListado;
    private JTable tblDatos;
    private JButton btnRefrescar;

    private final ControladorPedidos controlador;
    private DefaultTableModel modeloTabla;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public VentanaListaPedidos(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Listado General");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 520);
        setLocationRelativeTo(null);

        cmbListado.setModel(new DefaultComboBoxModel<>(new String[]{"Pedidos", "Repartidores", "Entregas"}));
        cmbListado.addActionListener(e -> refrescarTabla(true));
        btnRefrescar.addActionListener(e -> refrescarTabla(true));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refrescarTabla(false);
            }
        });

        refrescarTabla(true);
    }

    private void refrescarTabla(boolean mostrarError) {
        try {
            String opcion = (String) cmbListado.getSelectedItem();
            if ("Repartidores".equals(opcion)) {
                mostrarRepartidores();
            } else if ("Entregas".equals(opcion)) {
                mostrarEntregas();
            } else {
                mostrarPedidos();
            }
            tblDatos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tblDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarPedidos() {
        modeloTabla = modeloNoEditable(new String[]{"ID", "Dirección", "Tipo", "Estado", "Último repartidor"});
        for (Pedido pedido : controlador.getPedidos()) {
            modeloTabla.addRow(new Object[]{pedido.getIdPedido(), pedido.getDireccionEntrega(), pedido.getTipoPedido(), pedido.getEstado(), pedido.getRepartidorAsignado()});
        }
        tblDatos.setModel(modeloTabla);
    }

    private void mostrarRepartidores() {
        modeloTabla = modeloNoEditable(new String[]{"ID", "Nombre"});
        for (Repartidor repartidor : controlador.getRepartidores()) {
            modeloTabla.addRow(new Object[]{repartidor.getId(), repartidor.getNombre()});
        }
        tblDatos.setModel(modeloTabla);
    }

    private void mostrarEntregas() {
        modeloTabla = modeloNoEditable(new String[]{"ID", "ID Pedido", "Dirección", "ID Repartidor", "Repartidor", "Fecha", "Hora"});
        for (Entrega entrega : controlador.getEntregas()) {
            modeloTabla.addRow(new Object[]{entrega.getId(), entrega.getIdPedido(), entrega.getDireccionPedido(), entrega.getIdRepartidor(), entrega.getNombreRepartidor(), entrega.getFecha() == null ? "" : entrega.getFecha(), entrega.getHora() == null ? "" : entrega.getHora().format(FORMATO_HORA)});
        }
        tblDatos.setModel(modeloTabla);
    }

    private DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
