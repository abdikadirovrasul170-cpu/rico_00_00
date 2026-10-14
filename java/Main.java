import java.util.ArrayList;
import java.util.List;

// Singleton: бүкіл бағдарламаға ортақ орталық.
class LogisticsCenter {
    private static final LogisticsCenter INSTANCE = new LogisticsCenter();
    private final List<Cargo> cargos = new ArrayList<>();
    private LogisticsCenter() {}
    public static LogisticsCenter getInstance() { return INSTANCE; }
    public void addCargo(Cargo cargo) { cargos.add(cargo); }
    public int getCargoCount() { return cargos.size(); }
}

// Observer: мәртебе өзгергенде клиент хабар алады.
interface CargoObserver {
    void update(String id, String status);
}
class Client implements CargoObserver {
    private final String name;
    Client(String name) { this.name = name; }
    public void update(String id, String status) {
        System.out.println("Хабарлама → " + name + ": " + id + " — " + status);
    }
}

// State: әр күй келесі ауысуды анықтайды.
interface CargoState {
    String getName();
    void next(Cargo cargo);
}
class RegisteredState implements CargoState {
    public String getName() { return "Тіркелді"; }
    public void next(Cargo cargo) { cargo.setState(new ReadyState()); }
}
class ReadyState implements CargoState {
    public String getName() { return "Ұшуға дайын"; }
    public void next(Cargo cargo) { cargo.setState(new InTransitState()); }
}
class InTransitState implements CargoState {
    public String getName() { return "Жолда"; }
    public void next(Cargo cargo) { cargo.setState(new DeliveredState()); }
}
class DeliveredState implements CargoState {
    public String getName() { return "Жеткізілді"; }
    public void next(Cargo cargo) {
        System.out.println(cargo.getId() + ": жүк жеткізілген, келесі күй жоқ.");
    }
}
class Cargo {
    private final String id;
    private final double weight;
    private final String route;
    private CargoState state = new RegisteredState();
    private final List<CargoObserver> observers = new ArrayList<>();
    Cargo(String id, double weight, String route) {
        if (!Double.isFinite(weight) || weight <= 0)
            throw new IllegalArgumentException("Салмақ оң және шекті сан болуы керек.");
        this.id = id;
        this.weight = weight;
        this.route = route;
    }
    public String getId() { return id; }
    public double getWeight() { return weight; }
    public String getRoute() { return route; }
    public String getStatus() { return state.getName(); }
    public void addObserver(CargoObserver observer) { observers.add(observer); }
    public void removeObserver(CargoObserver observer) { observers.remove(observer); }
    public void notifyObservers() {
        for (CargoObserver observer : observers) observer.update(id, getStatus());
    }
    public void setState(CargoState state) {
        this.state = state;
        notifyObservers();
    }
    public void nextState() { state.next(this); }
}

// Strategy: стандартты және жедел баға алгоритмдері.
interface DeliveryStrategy {
    double calculatePrice(double weight);
    String getName();
}
class StandardDelivery implements DeliveryStrategy {
    public double calculatePrice(double weight) { return weight * 1500; }
    public String getName() { return "Стандартты жеткізу"; }
}
class ExpressDelivery implements DeliveryStrategy {
    public double calculatePrice(double weight) { return weight * 2500; }
    public String getName() { return "Жедел жеткізу"; }
}

// Facade: ішкі операцияларға қарапайым интерфейс.
class AirLogisticsFacade {
    private final LogisticsCenter center = LogisticsCenter.getInstance();
    public Cargo registerCargo(String id, double weight, String route,
                               CargoObserver client, DeliveryStrategy strategy) {
        Cargo cargo = new Cargo(id, weight, route);
        cargo.addObserver(client);
        center.addCargo(cargo);
        System.out.println("\nЖүк нөмірі: " + id);
        System.out.println("Бағыт: " + cargo.getRoute());
        System.out.println("Салмақ: " + weight + " кг");
        showPrice(cargo, strategy);
        cargo.notifyObservers();
        return cargo;
    }
    public void showPrice(Cargo cargo, DeliveryStrategy strategy) {
        System.out.println("Жеткізу түрі: " + strategy.getName());
        System.out.printf("Жеткізу құны: %.0f ₸%n", strategy.calculatePrice(cargo.getWeight()));
    }
    public void advanceCargo(Cargo cargo) { cargo.nextState(); }
    public void showCargoCount() {
        System.out.println("\nТіркелген жүк саны: " + center.getCargoCount());
    }
}
public class Main {
    public static void main(String[] args) {
        AirLogisticsFacade logistics = new AirLogisticsFacade();
        Cargo cargo = logistics.registerCargo("AIR-001", 10,
            "Алматы → Ташкент", new Client("Расул"), new StandardDelivery());
        System.out.println("\nЖедел жеткізу бағасын салыстыру:");
        logistics.showPrice(cargo, new ExpressDelivery());
        System.out.println("\nЖүктің қозғалысы:");
        for (int i = 0; i < 4; i++) logistics.advanceCargo(cargo);
        logistics.showCargoCount();
    }
}
