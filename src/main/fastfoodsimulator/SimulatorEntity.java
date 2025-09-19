package main.fastfoodsimulator;

public abstract class SimulatorEntity {
    private int id;
    private String status;

    public SimulatorEntity(int id) {
        this.id = id;
        this.status = "Idle";
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    // Абстрактный метод для полиморфизма: каждая сущность будет реализовывать свою логику работы
    public abstract void performAction();
}