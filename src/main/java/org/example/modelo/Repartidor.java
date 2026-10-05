package org.example.modelo;

public class Repartidor implements Runnable {
    private int id;
    private String nombre;
    private final Pedido pedido;
    private final Runnable alFinalizar;

    public Repartidor(int id, String nombre) {
        this(id, nombre, null, null);
    }

    public Repartidor(String nombre) {
        this(0, nombre, null, null);
    }

    public Repartidor(String nombre, Pedido pedido) {
        this(0, nombre, pedido, null);
    }

    public Repartidor(String nombre, Pedido pedido, Runnable alFinalizar) {
        this(0, nombre, pedido, alFinalizar);
    }

    private Repartidor(int id, String nombre, Pedido pedido, Runnable alFinalizar) {
        this.id = id;
        this.nombre = nombre;
        this.pedido = pedido;
        this.alFinalizar = alFinalizar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Pedido getPedido() {
        return pedido;
    }

    @Override
    public String toString() {
        return "#" + id + " - " + nombre;
    }

    @Override
    public void run() {
        if (pedido == null) {
            return;
        }
        try {
            System.out.println(nombre + " inició la entrega del pedido #" + pedido.getIdPedido());
            Thread.sleep(pedido.getDuracionSimulacionMs());
            pedido.setEstado(EstadoPedido.ENTREGADO);
            System.out.println(nombre + " entregó el pedido #" + pedido.getIdPedido());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.asignarRepartidor("Sin asignar");
            System.out.println("La entrega del pedido #" + pedido.getIdPedido() + " fue interrumpida.");
        } finally {
            if (alFinalizar != null) {
                alFinalizar.run();
            }
        }
    }
}
