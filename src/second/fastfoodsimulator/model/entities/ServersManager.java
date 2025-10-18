package second.fastfoodsimulator.model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServersManager {
    private final List<Server> servers;
    private final AtomicInteger activeServers;

    public ServersManager(int serversCount) {
        this.servers = new ArrayList<>();
        this.activeServers = new AtomicInteger(0);

        for (int i = 1; i <= serversCount; i++) {
            servers.add(new Server(i));
        }
        System.out.println("Создано " + serversCount + " официантов");
    }

    public synchronized Server getAvailableServer() {
        for (Server server : servers) {
            synchronized (server) {
                if (!server.isBusy()) {
                    return server;
                }
            }
        }
        return null;
    }

    public synchronized List<Server> getServers() {
        return new ArrayList<>(servers);
    }

    public synchronized int getBusyServersCount() {
        int count = 0;
        for (Server server : servers) {
            synchronized (server) {
                if (server.isBusy()) {
                    count++;
                }
            }
        }
        activeServers.set(count);
        return count;
    }

    public synchronized int getTotalServersCount() {
        return servers.size();
    }

    public synchronized int getActiveServersCount() {
        return activeServers.get();
    }

    public synchronized void reset() {
        for (Server server : servers) {
            synchronized (server) {
                server.completeServing();
            }
        }
        activeServers.set(0);
        System.out.println("Состояние всех официантов сброшено");
    }
}