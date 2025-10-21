package second.fastfoodsimulator.model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CooksManager {
    private final List<Cook> cooks;
    private final AtomicInteger activeCooks;

    public CooksManager(int cooksCount) {
        this.cooks = new ArrayList<>();
        this.activeCooks = new AtomicInteger(0);

        for (int i = 1; i <= cooksCount; i++) {
            cooks.add(new Cook(i));
        }
        System.out.println("Создано " + cooksCount + " поваров");
    }

    public synchronized Cook getAvailableCook() {
        for (Cook cook : cooks) {
            synchronized (cook) {
                if (!cook.isBusy()) {
                    return cook;
                }
            }
        }
        return null;
    }

    public synchronized List<Cook> getCooks() {
        return new ArrayList<>(cooks);
    }

    public synchronized int getBusyCooksCount() {
        int count = 0;
        for (Cook cook : cooks) {
            synchronized (cook) {
                if (cook.isBusy()) {
                    count++;
                }
            }
        }
        activeCooks.set(count);
        return count;
    }

    public synchronized int getTotalCooksCount() {
        return cooks.size();
    }

    public synchronized void reset() {
        for (Cook cook : cooks) {
            synchronized (cook) {
                cook.completeCooking();
            }
        }
        activeCooks.set(0);
        System.out.println("Состояние всех поваров сброшено");
    }
}