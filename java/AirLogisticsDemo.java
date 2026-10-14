import java.util.*;

/** Авиа логистика: кестедегі төрт топтың орындалатын мысалдары. Java 11+. */
public class AirLogisticsDemo {
    // OBSERVER — жүк өзгерістеріне жазылу.
    interface Observer { void update(String message); }
    static class Client implements Observer {
        final String name;
        Client(String name) { this.name = name; }
        public void update(String message) { System.out.println(name + " ← " + message); }
    }
    // STATE — келесі күйді әр күй өзі анықтайды.
    interface State { String name(); State next(); }
    static class Registered implements State {
        public String name() { return "Тіркелді"; }
        public State next() { return new Ready(); }
    }
    static class Ready implements State {
        public String name() { return "Ұшуға дайын"; }
        public State next() { return new Transit(); }
    }
    static class Transit implements State {
        public String name() { return "Жолда"; }
        public State next() { return new Delivered(); }
    }
    static class Delivered implements State {
        public String name() { return "Жеткізілді"; }
        public State next() { return this; }
    }
    static class Cargo {
        final String id, type;
        final double weight;
        State state = new Registered();
        final List<Observer> observers = new ArrayList<>();
        Cargo(String id, double weight, String type) {
            if (!Double.isFinite(weight) || weight <= 0)
                throw new IllegalArgumentException("Салмақ оң және шекті сан болуы керек.");
            this.id = id; this.weight = weight; this.type = type;
        }
        void subscribe(Observer observer) { observers.add(observer); }
        void notifyObservers() {
            for (Observer o : observers) o.update(id + " — " + type + " — " + state.name());
        }
        void advance() {
            State next = state.next();
            if (next == state) { System.out.println(id + ": жеткізу аяқталған."); return; }
            state = next; notifyObservers();
        }
    }
    // SINGLETON — бір ғана логистика орталығы.
    static class Center {
        private static final Center INSTANCE = new Center();
        final List<Cargo> cargos = new ArrayList<>();
        private Center() {}
        static Center getInstance() { return INSTANCE; }
    }
    // STRATEGY — баға есептеу тәсілдері.
    interface Pricing { double calculate(double weight); }
    static class StandardPricing implements Pricing {
        public double calculate(double weight) { return weight * 1500; }
    }
    static class ExpressPricing implements Pricing {
        public double calculate(double weight) { return weight * 2500; }
    }
    // FACADE — ортақ қарапайым интерфейс.
    static class Facade {
        final Center center = Center.getInstance();
        Cargo register(String id, double weight, Observer client, Pricing pricing) {
            return accept(new Cargo(id, weight, "Стандартты жүк"), client, pricing);
        }
        Cargo accept(Cargo cargo, Observer client, Pricing pricing) {
            cargo.subscribe(client); center.cargos.add(cargo);
            System.out.printf("%s құны: %.0f ₸%n", cargo.id, pricing.calculate(cargo.weight));
            cargo.notifyObservers(); return cargo;
        }
        void advance(Cargo cargo) { cargo.advance(); }
    }
    // FACTORY METHOD — мұрагерлер қандай жүк жасалатынын шешеді.
    abstract static class CargoCreator {
        abstract Cargo createCargo(String id, double weight);
        Cargo prepare(String id, double weight) {
            Cargo cargo = createCargo(id, weight);
            System.out.println("Factory Method жасады: " + cargo.type);
            return cargo;
        }
    }
    static class StandardCreator extends CargoCreator {
        Cargo createCargo(String id, double weight) { return new Cargo(id, weight, "Стандартты жүк"); }
    }
    static class ExpressCreator extends CargoCreator {
        Cargo createCargo(String id, double weight) { return new Cargo(id, weight, "Жедел жүк"); }
    }
    // MEDIATOR — бөлімдер бір-біріне емес, үйлестірушіге жүгінеді.
    interface Mediator { void send(Department sender, Cargo cargo); }
    static class Department {
        final String name; final Mediator mediator;
        Department(String name, Mediator mediator) { this.name = name; this.mediator = mediator; }
        void report(Cargo cargo) { mediator.send(this, cargo); }
        void receive(String message) { System.out.println(name + ": " + message); }
    }
    static class AirportMediator implements Mediator {
        final List<Department> departments = new ArrayList<>();
        void add(Department department) { departments.add(department); }
        public void send(Department sender, Cargo cargo) {
            for (Department d : departments)
                if (d != sender) d.receive(sender.name + " хабарлады: " + cargo.id + " қабылданды");
        }
    }
    // ABSTRACT FACTORY — өзара үйлесімді өнімдер отбасын жасайды.
    interface Packaging { String name(); }
    interface Document { String name(); }
    interface ShipmentFactory { Packaging createPackaging(); Document createDocument(); }
    static class DomesticBox implements Packaging { public String name() { return "Ішкі рейс қаптамасы"; } }
    static class DomesticDocument implements Document { public String name() { return "Ішкі тасымал құжаты"; } }
    static class InternationalBox implements Packaging { public String name() { return "Халықаралық қаптама"; } }
    static class InternationalDocument implements Document { public String name() { return "Халықаралық тасымал құжаты"; } }
    static class DomesticFactory implements ShipmentFactory {
        public Packaging createPackaging() { return new DomesticBox(); }
        public Document createDocument() { return new DomesticDocument(); }
    }
    static class InternationalFactory implements ShipmentFactory {
        public Packaging createPackaging() { return new InternationalBox(); }
        public Document createDocument() { return new InternationalDocument(); }
    }
    // CHAIN OF RESPONSIBILITY — тексерулер ретімен өтеді, қате болса тоқтайды.
    static class Request {
        final Cargo cargo; final boolean documentsPresent; final boolean dangerous;
        Request(Cargo cargo, boolean documentsPresent, boolean dangerous) {
            this.cargo = cargo; this.documentsPresent = documentsPresent; this.dangerous = dangerous;
        }
    }
    abstract static class Handler {
        Handler next;
        Handler link(Handler handler) { next = handler; return handler; }
        final boolean handle(Request request) {
            if (!check(request)) return false;
            return next == null || next.handle(request);
        }
        abstract boolean check(Request request);
    }
    static class DocumentCheck extends Handler {
        boolean check(Request r) {
            System.out.println("Құжат: " + (r.documentsPresent ? "бар" : "жоқ — тоқтатылды"));
            return r.documentsPresent;
        }
    }
    static class WeightCheck extends Handler {
        boolean check(Request r) {
            boolean valid = r.cargo.weight <= 1000;
            System.out.println("Салмақ: " + (valid ? "рұқсат" : "лимиттен асты — тоқтатылды"));
            return valid;
        }
    }
    static class SafetyCheck extends Handler {
        boolean check(Request r) {
            System.out.println("Қауіпсіздік: " + (!r.dangerous ? "рұқсат" : "арнайы тексеру керек — тоқтатылды"));
            return !r.dangerous;
        }
    }
    // COMMAND — әрекетті жеке объектіге айналдырады.
    interface Command { void execute(); }
    static class AdvanceCommand implements Command {
        final Cargo cargo;
        AdvanceCommand(Cargo cargo) { this.cargo = cargo; }
        public void execute() { cargo.advance(); }
    }
    static class Dispatcher {
        final Queue<Command> queue = new ArrayDeque<>();
        void add(Command command) { queue.add(command); }
        void run() { while (!queue.isEmpty()) queue.remove().execute(); }
    }
    static void group1() {
        System.out.println("\n1-ТОП: Singleton, Observer, State, Strategy, Facade");
        Facade facade = new Facade();
        Cargo cargo = facade.register("AIR-001", 10, new Client("Расул"), new StandardPricing());
        System.out.println("Жедел баға: " + new ExpressPricing().calculate(cargo.weight));
        for (int i = 0; i < 4; i++) facade.advance(cargo);
        System.out.println("Орталық бірдей: " + (Center.getInstance() == Center.getInstance()));
    }
    static void group2() {
        System.out.println("\n2-ТОП: Factory Method, State, Observer, Facade, Mediator");
        CargoCreator creator = new ExpressCreator();
        Cargo cargo = new Facade().accept(creator.prepare("AIR-002", 12), new Client("Ануар"), new ExpressPricing());
        AirportMediator mediator = new AirportMediator();
        Department warehouse = new Department("Қойма", mediator);
        Department flight = new Department("Рейс бөлімі", mediator);
        Department delivery = new Department("Жеткізу бөлімі", mediator);
        mediator.add(warehouse); mediator.add(flight); mediator.add(delivery);
        warehouse.report(cargo);
        cargo.advance(); cargo.advance(); cargo.advance();
    }
    static void group3() {
        System.out.println("\n3-ТОП: Abstract Factory, State, Strategy, Observer, Facade");
        ShipmentFactory factory = new InternationalFactory();
        System.out.println(factory.createPackaging().name());
        System.out.println(factory.createDocument().name());
        Cargo cargo = new Facade().register("AIR-003", 8, new Client("Аружан"), new ExpressPricing());
        cargo.advance(); cargo.advance(); cargo.advance();
    }
    static void group4() {
        System.out.println("\n4-ТОП: Chain of Responsibility, State, Command, Observer");
        Cargo cargo = new Cargo("AIR-004", 20, "Тексерілетін жүк");
        cargo.subscribe(new Client("Клиент"));
        Handler chain = new DocumentCheck();
        chain.link(new WeightCheck()).link(new SafetyCheck());
        if (chain.handle(new Request(cargo, true, false))) {
            Dispatcher dispatcher = new Dispatcher();
            for (int i = 0; i < 3; i++) dispatcher.add(new AdvanceCommand(cargo));
            dispatcher.run();
        }
        System.out.println("\nҚұжатсыз сұраныс:");
        chain.handle(new Request(new Cargo("AIR-005", 5, "Жүк"), false, false));
    }
    public static void main(String[] args) {
        if (args.length == 0) { group1(); group2(); group3(); group4(); return; }
        switch (args[0]) {
            case "1": group1(); break;
            case "2": group2(); break;
            case "3": group3(); break;
            case "4": group4(); break;
            default: System.out.println("Топ нөмірі 1, 2, 3 немесе 4 болуы керек.");
        }
    }
}
