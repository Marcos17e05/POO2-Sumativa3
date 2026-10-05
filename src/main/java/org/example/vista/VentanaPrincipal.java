package org.example.vista;

import org.example.controlador.ControladorPedidos;

import javax.swing.*;

public class VentanaPrincipal extends JFrame {
    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JButton btnRegistrarPedido;
    private JButton btnRegistrarRepartidor;
    private JButton btnListarPedidos;
    private JButton btnAsignarEntrega;

    private final ControladorPedidos controlador;

    public VentanaPrincipal() {
        this(new ControladorPedidos());
    }

    public VentanaPrincipal(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Gestión de Entregas");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        btnRegistrarPedido.addActionListener(e -> abrirGestionPedidos());
        btnRegistrarRepartidor.addActionListener(e -> abrirGestionRepartidores());
        btnListarPedidos.addActionListener(e -> abrirListado());
        btnAsignarEntrega.addActionListener(e -> abrirGestionEntregas());
    }

    private void abrirGestionPedidos() {
        VentanaRegistroPedido ventana = new VentanaRegistroPedido(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirGestionRepartidores() {
        VentanaRegistroRepartidor ventana = new VentanaRegistroRepartidor(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirListado() {
        VentanaListaPedidos ventana = new VentanaListaPedidos(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirGestionEntregas() {
        VentanaAsignacionEntrega ventana = new VentanaAsignacionEntrega(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }
}
