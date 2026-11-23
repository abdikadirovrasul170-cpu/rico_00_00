import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * CareerBoost — алты GoF паттернінің Java консольдік демонстрациясы.
 * Java 17+; сыртқы кітапхана қажет емес.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================");
        System.out.println("       CAREERBOOST — JAVA DEMO");
        System.out.println("======================================");
        System.out.println("Бұл бағдарлама 6 паттернді көрсетеді:");
        System.out.println("1. Builder");
        System.out.println("2. Abstract Factory");
        System.out.println("3. Template Method");
        System.out.println("4. Strategy");
        System.out.println("5. Adapter");
        System.out.println("6. Visitor");
        System.out.println("0. Барлығын орындау");
        System.out.print("\nТаңдауыңыз: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> builderDemo();
            case "2" -> abstractFactoryDemo();
            case "3" -> templateMethodDemo();
            case "4" -> strategyDemo();
            case "5" -> adapterDemo();
            case "6" -> visitorDemo();
            case "0" -> {
                builderDemo();
                abstractFactoryDemo();
                templateMethodDemo();
                strategyDemo();
                adapterDemo();
                visitorDemo();
            }
            default -> System.out.println("Қате таңдау. Бағдарламаны қайта іске қосыңыз.");
        }

        System.out.println("\nCareerBoost демонстрациясы аяқталды.");
        scanner.close();
    }

    // =========================================================
    // 1. BUILDER
    // Мақсаты: күрделі Profile объектісін кезең-кезеңімен құру.
    // =========================================================
    static class Profile {
        String name;
        String email;
        String skills;
        int experienceYears;

        Profile(String name, String email, String skills, int experienceYears) {
            this.name = name;
            this.email = email;
            this.skills = skills;
            this.experienceYears = experienceYears;
        }

        void show() {
            System.out.println("Аты: " + name);
            System.out.println("Email: " + email);
            System.out.println("Дағдылар: " + skills);
            System.out.println("Тәжірибе: " + experienceYears + " жыл");
        }
    }

    static class ProfileBuilder {
        private String name;
        private String email;
        private String skills = "Көрсетілмеген";
        private int experienceYears = 0;

        ProfileBuilder name(String name) {
            this.name = name;
            return this;
        }

        ProfileBuilder email(String email) {
            this.email = email;
            return this;
        }

        ProfileBuilder skills(String skills) {
            this.skills = skills;
            return this;
        }

        ProfileBuilder experience(int years) {
            this.experienceYears = years;
            return this;
        }

        Profile build() {
            if (name == null || name.isBlank() ||
                email == null || email.isBlank()) {
                throw new IllegalStateException("Аты мен email міндетті!");
            }
            return new Profile(name, email, skills, experienceYears);
        }
    }

    static void builderDemo() {
        heading("1. BUILDER");
        System.out.println("Мақсаты: объектіні бірден емес, қадамдап құру.");
        Profile profile = new ProfileBuilder()
                .name("Расул Абдикадиров")
                .email("rasul@example.com")
                .skills("Java, OOP, SQL")
                .experience(1)
                .build();
        System.out.println("Нәтиже — профиль сәтті құрылды:");
        profile.show();
    }

    // =========================================================
    // 2. ABSTRACT FACTORY
    // Мақсаты: бір-бірімен байланысты объектілер отбасын құру.
    // =========================================================
    interface Dashboard {
        void render();
    }

    interface Menu {
        void render();
    }

    interface UserInterfaceFactory {
        Dashboard createDashboard();
        Menu createMenu();
    }

    static class CandidateDashboard implements Dashboard {
        public void render() {
            System.out.println("Кандидат панелі: Менің түйіндемем, Вакансиялар");
        }
    }

    static class CandidateMenu implements Menu {
        public void render() {
            System.out.println("Кандидат мәзірі: Профиль | Іздеу | Өтінімдер");
        }
    }

    static class RecruiterDashboard implements Dashboard {
        public void render() {
            System.out.println("Рекрутер панелі: Компания, Жарияланған вакансиялар");
        }
    }

    static class RecruiterMenu implements Menu {
        public void render() {
            System.out.println("Рекрутер мәзірі: Вакансия қосу | Үміткерлер");
        }
    }

    static class CandidateFactory implements UserInterfaceFactory {
        public Dashboard createDashboard() { return new CandidateDashboard(); }
        public Menu createMenu() { return new CandidateMenu(); }
    }

    static class RecruiterFactory implements UserInterfaceFactory {
        public Dashboard createDashboard() { return new RecruiterDashboard(); }
        public Menu createMenu() { return new RecruiterMenu(); }
    }

    static void abstractFactoryDemo() {
        heading("2. ABSTRACT FACTORY");
        System.out.println("Мақсаты: бір рөлге арналған үйлесімді объектілер тобын құру.");
        UserInterfaceFactory factory = new CandidateFactory();
        System.out.println("Таңдалған рөл: Candidate");
        factory.createDashboard().render();
        factory.createMenu().render();

        System.out.println("\nРекрутер рөлі:");
        UserInterfaceFactory recruiterFactory = new RecruiterFactory();
        recruiterFactory.createDashboard().render();
        recruiterFactory.createMenu().render();
    }

    // =========================================================
    // 3. TEMPLATE METHOD
    // Мақсаты: алгоритм қадамдарының ретін базалық класта бекіту.
    // =========================================================
    static abstract class ResumeReview {
        public final void review() {
            System.out.println("1-қадам: Түйіндеме деректерін оқу");
            checkContacts();
            checkSkills();
            System.out.println("4-қадам: Қорытынды шығару");
        }

        protected void checkContacts() {
            System.out.println("2-қадам: Байланыс деректері тексерілді");
        }

        protected abstract void checkSkills();
    }

    static class CandidateResumeReview extends ResumeReview {
        @Override
        protected void checkSkills() {
            System.out.println("3-қадам: Java және SQL дағдылары тексерілді");
        }
    }

    static void templateMethodDemo() {
        heading("3. TEMPLATE METHOD");
        System.out.println("Мақсаты: алгоритм ретін сақтап, кейбір қадамдарды өзгерту.");
        ResumeReview review = new CandidateResumeReview();
        review.review();
    }

    // =========================================================
    // 4. STRATEGY
    // Мақсаты: алгоритмді орындалу кезінде ауыстыру.
    // =========================================================
    static class Vacancy {
        String title;
        String company;
        int salary;

        Vacancy(String title, String company, int salary) {
            this.title = title;
            this.company = company;
            this.salary = salary;
        }

        public String toString() {
            return title + " | " + company + " | " + salary + " KZT";
        }
    }

    interface SortStrategy {
        List<Vacancy> sort(List<Vacancy> vacancies);
    }

    static class SalarySortStrategy implements SortStrategy {
        public List<Vacancy> sort(List<Vacancy> vacancies) {
            List<Vacancy> result = new ArrayList<>(vacancies);
            result.sort(Comparator.comparingInt((Vacancy v) -> v.salary).reversed());
            return result;
        }
    }

    static class TitleSortStrategy implements SortStrategy {
        public List<Vacancy> sort(List<Vacancy> vacancies) {
            List<Vacancy> result = new ArrayList<>(vacancies);
            result.sort(Comparator.comparing(v -> v.title));
            return result;
        }
    }

    static class VacancyCatalog {
        private SortStrategy strategy;

        VacancyCatalog(SortStrategy strategy) {
            this.strategy = strategy;
        }

        void setStrategy(SortStrategy strategy) {
            this.strategy = strategy;
        }

        void showSorted(List<Vacancy> vacancies) {
            strategy.sort(vacancies).forEach(System.out::println);
        }
    }

    static void strategyDemo() {
        heading("4. STRATEGY");
        System.out.println("Мақсаты: вакансияны сұрыптау алгоритмін ауыстыру.");
        List<Vacancy> vacancies = List.of(
                new Vacancy("Java Developer", "Tech KZ", 650000),
                new Vacancy("QA Engineer", "Quality Lab", 420000),
                new Vacancy("Backend Developer", "Digital Group", 750000)
        );

        VacancyCatalog catalog = new VacancyCatalog(new SalarySortStrategy());
        System.out.println("Жалақы бойынша (кему ретімен):");
        catalog.showSorted(vacancies);

        catalog.setStrategy(new TitleSortStrategy());
        System.out.println("\nАтауы бойынша (алфавитпен):");
        catalog.showSorted(vacancies);
    }

    // =========================================================
    // 5. ADAPTER
    // Мақсаты: үйлеспейтін сыртқы интерфейсті ішкі интерфейске
    // бейімдеу.
    // =========================================================
    interface VacancySource {
        Vacancy getVacancy();
    }

    static class LegacyJobApi {
        String fetchOldJobData() {
            return "Data Analyst;Data Company;500000";
        }
    }

    static class LegacyJobAdapter implements VacancySource {
        private final LegacyJobApi oldApi;

        LegacyJobAdapter(LegacyJobApi oldApi) {
            this.oldApi = oldApi;
        }

        public Vacancy getVacancy() {
            String[] parts = oldApi.fetchOldJobData().split(";");
            return new Vacancy(parts[0], parts[1], Integer.parseInt(parts[2]));
        }
    }

    static void adapterDemo() {
        heading("5. ADAPTER");
        System.out.println("Мақсаты: ескі API дерегін CareerBoost форматына айналдыру.");
        LegacyJobApi externalApi = new LegacyJobApi();
        VacancySource adapter = new LegacyJobAdapter(externalApi);
        Vacancy vacancy = adapter.getVacancy();
        System.out.println("Сыртқы жүйеден алынған және бейімделген вакансия:");
        System.out.println(vacancy);
    }

    // =========================================================
    // 6. VISITOR
    // Мақсаты: объектілер кластарын өзгертпей жаңа операция қосу.
    // =========================================================
    interface CareerElement {
        void accept(CareerVisitor visitor);
    }

    interface CareerVisitor {
        void visitProfile(Profile profile);
        void visitVacancy(Vacancy vacancy);
    }

    static class ProfileElement implements CareerElement {
        private final Profile profile;

        ProfileElement(Profile profile) {
            this.profile = profile;
        }

        public void accept(CareerVisitor visitor) {
            visitor.visitProfile(profile);
        }
    }

    static class VacancyElement implements CareerElement {
        private final Vacancy vacancy;

        VacancyElement(Vacancy vacancy) {
            this.vacancy = vacancy;
        }

        public void accept(CareerVisitor visitor) {
            visitor.visitVacancy(vacancy);
        }
    }

    static class CareerReportVisitor implements CareerVisitor {
        public void visitProfile(Profile profile) {
            int count = profile.skills.equals("Көрсетілмеген")
                    ? 0 : profile.skills.split(",").length;
            System.out.println("Профиль есебі: " + profile.name);
            System.out.println("Дағдылар саны: " + count);
            System.out.println("Тәжірибесі: " + profile.experienceYears + " жыл");
        }

        public void visitVacancy(Vacancy vacancy) {
            System.out.println("Вакансия есебі: " + vacancy.title);
            System.out.println("Компания: " + vacancy.company);
            System.out.println("Ұсынылатын жалақы: " + vacancy.salary + " KZT");
        }
    }

    static void visitorDemo() {
        heading("6. VISITOR");
        System.out.println("Мақсаты: негізгі кластарды өзгертпей есеп операциясын қосу.");
        Profile profile = new ProfileBuilder()
                .name("Расул")
                .email("rasul@example.com")
                .skills("Java, SQL, Git")
                .experience(1)
                .build();

        Vacancy vacancy = new Vacancy("Java Developer", "Tech KZ", 650000);
        CareerVisitor report = new CareerReportVisitor();

        CareerElement[] elements = {
                new ProfileElement(profile),
                new VacancyElement(vacancy)
        };

        for (CareerElement element : elements) {
            element.accept(report);
            System.out.println("------------------------------");
        }
    }

    static void heading(String title) {
        System.out.println("\n======================================");
        System.out.println(title);
        System.out.println("======================================");
    }
}
