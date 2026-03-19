package cli;

import repo.*;
import service.*;
import model.*;
import util.ValidationException;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static final String DATA_DIR = "data" + File.separator;
    private static final String VEHICLE_DB = DATA_DIR + "vehicles.db";
    private static final String RIDE_DB = DATA_DIR + "rides.db";
    private static final String HISTORY_DB = DATA_DIR + "ride_history.db";
    private static UserRepository userRepo;
    private static VehicleRepository vehicleRepo;
    private static RideRepository rideRepo;
    private static RideHistoryRepository historyRepo;
    private static AuthService auth;
    private static RideService rideService;
    private static PricingService pricingService;
    private static RatingService ratingService;
    private static RideHistoryService historyService;
    private static Scanner sc;
    private static DeliveryRepository deliveryRepo;
    private static RestaurantRepository restaurantRepo;
    private static OrderRepository orderRepo;
    private static AvaliacaoRepository avaliacaoRepo;
    private static DeliveryService deliveryService;
    private static RestaurantService restaurantService;
    private static OrderService orderService;
    private static AvaliacaoService avaliacaoService;
    private static NotificationService notificationService;
    private static DeliveryAssignmentService assignmentService;

    public static void main(String[] args) {
        userRepo = new UserRepository();
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists())
            dataDir.mkdirs();
        vehicleRepo = new VehicleRepository(VEHICLE_DB);
        rideRepo = new RideRepository(RIDE_DB);
        historyRepo = new RideHistoryRepository(HISTORY_DB);
        auth = new AuthService(userRepo, vehicleRepo);
        pricingService = new PricingService();
        rideService = new RideService(rideRepo, userRepo, pricingService);
        rideService.setHistoryRepository(historyRepo);
        ratingService = new RatingService(userRepo, rideRepo);
        historyService = new RideHistoryService(historyRepo, userRepo);
        sc = new Scanner(System.in);
        deliveryRepo = new DeliveryRepository();
        restaurantRepo = new RestaurantRepository();
        orderRepo = new OrderRepository();
        avaliacaoRepo = new AvaliacaoRepository();

        deliveryService = new DeliveryService(deliveryRepo);
        restaurantService = new RestaurantService(restaurantRepo);
        orderService = new OrderService(orderRepo, restaurantRepo, restaurantService);
        avaliacaoService = new AvaliacaoService(avaliacaoRepo, orderRepo);
        notificationService = new NotificationService();
        assignmentService = new DeliveryAssignmentService(deliveryRepo, notificationService);
        orderService.setNotificationService(notificationService);

        MenuContext context = new MenuContext(userRepo, vehicleRepo, rideRepo, historyRepo,
                deliveryRepo, restaurantRepo, orderRepo, avaliacaoRepo, auth, rideService, pricingService,
                ratingService, historyService, deliveryService, restaurantService, orderService, avaliacaoService,
                notificationService, assignmentService, sc);

        System.out.println("=== UberPB ===");
        while (true) {
            System.out.println("\n=== Menu Principal ===");
            System.out.println("1 - Cadastrar Passageiro");
            System.out.println("2 - Cadastrar Motorista");
            System.out.println("3 - Cadastrar Entregador");
            System.out.println("4 - Cadastrar Restaurante");
            System.out.println("5 - Fazer Login");
            System.out.println("6 - Listar Usuários (Admin)");
            System.out.println("7 - Listar Categorias de Veículos");
            System.out.println("8 - Ajustar Tarifa Dinâmica (Admin)");
            System.out.println("0 - Sair");
            System.out.print("> ");
            String opt = sc.nextLine().trim();

            try {
                switch (opt) {
                    case "1":
                        registerPassenger();
                        break;
                    case "2":
                        registerDriver();
                        break;
                    case "3":
                        registerDelivery(context);
                        break;
                    case "4":
                        registerRestaurant(context);
                        break;
                    case "5":
                        loginUser(context);
                        break;
                    case "6":
                        listUsers();
                        break;
                    case "7":
                        listCategories();
                        break;
                    case "8":
                        adjustDynamicFare();
                        break;
                    case "0":
                        System.out.println("Saindo...");
                        sc.close();
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (ValidationException ve) {
                System.out.println("Erro de validação: " + ve.getMessage());
            } catch (IOException ioe) {
                System.out.println("Erro de I/O: " + ioe.getMessage());
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Erro: A entrada deve ser um número válido.");
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void loginUser(MenuContext context) {
        System.out.print("Email: ");
        String email = sc.nextLine().trim().toLowerCase();
        System.out.print("Senha: ");
        String password = sc.nextLine();

        // Criar perfil agregando todos os papéis do usuário
        UserProfile profile = new UserProfile(email);
        boolean foundAny = false;
        String authErrorMessage = null;

        // Verificar se existe como User (Passenger/Driver)
        try {
            User loggedInUser = auth.login(email, password);
            profile.setUser(loggedInUser);
            foundAny = true;
        } catch (ValidationException ve) {
            authErrorMessage = ve.getMessage();
            // Usuário não encontrado ou senha incorreta no userRepo
            // Continua verificando outros repositórios
        }

        // Verificar se existe como Delivery
        Delivery delivery = deliveryRepo.findByEmail(email).orElse(null);
        if (delivery != null) {
            // Entregadores não têm senha no modelo atual, apenas verificamos existência
            profile.setDelivery(delivery);
            foundAny = true;
        }

        // Verificar se existe como Restaurant
        Restaurant restaurant = restaurantRepo.findByEmail(email).orElse(null);
        if (restaurant != null) {
            String storedPassword = restaurant.getPassword();
            boolean passwordMatches = storedPassword != null
                    && (storedPassword.equals(password) || storedPassword.trim().equals(password.trim()));

            if (passwordMatches) {
                profile.setRestaurant(restaurant);
                foundAny = true;
            } else if (!foundAny) {
                authErrorMessage = "Senha incorreta para restaurante.";
            }
        }

        if (!foundAny) {
            if (authErrorMessage != null && !authErrorMessage.isBlank()) {
                System.out.println("Erro de login: " + authErrorMessage);
            } else {
                System.out.println("Erro de login: Email ou senha incorretos.");
            }
            return;
        }

        System.out.println("\nLogin bem-sucedido! Bem-vindo, " + profile.getPrimaryName() + "!");

        // Se tiver múltiplos papéis, mostrar menu de seleção
        if (profile.hasMultipleRoles()) {
            MultiRoleMenu multiRoleMenu = new MultiRoleMenu(context, profile);
            multiRoleMenu.show();
        } else {
            // Redirecionar diretamente para o menu apropriado
            if (profile.isPassenger()) {
                PassengerMenu passengerMenu = new PassengerMenu(context, profile.getAsPassenger());
                passengerMenu.show();
            } else if (profile.isDriver()) {
                DriverMenu driverMenu = new DriverMenu(context, profile.getAsDriver());
                driverMenu.show();
            } else if (profile.isDelivery()) {
                DeliveryMenu deliveryMenu = new DeliveryMenu(context, profile.getDelivery());
                deliveryMenu.show();
            } else if (profile.isRestaurant()) {
                RestaurantMenu restaurantMenu = new RestaurantMenu(context, profile.getRestaurant());
                restaurantMenu.show();
            }
        }
    }

    private static void registerPassenger() throws ValidationException, IOException {
        System.out.println("=== Cadastro de Passageiro ===");
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Telefone: ");
        String phone = sc.nextLine();
        System.out.print("Senha: ");
        String password = sc.nextLine();
        Passenger p = auth.registerPassenger(name, email, phone, password);
        System.out.println("\nPassageiro cadastrado com sucesso!");
        System.out.println("Nome: " + p.getName());
        System.out.println("Email: " + p.getEmail());
    }

    private static void registerDriver() throws ValidationException, IOException {
        System.out.println("=== Cadastro de Motorista ===");
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Telefone: ");
        String phone = sc.nextLine();
        System.out.print("Senha: ");
        String password = sc.nextLine();
        System.out.print("CNH (11 dígitos): ");
        String doc = sc.nextLine();
        System.out.print("Placa do veículo: ");
        String plate = sc.nextLine();
        System.out.print("Modelo do veículo: ");
        String model = sc.nextLine();
        System.out.print("Ano do veículo: ");
        int year = Integer.parseInt(sc.nextLine());
        System.out.print("Cor do veículo: ");
        String color = sc.nextLine();
        if (plate == null || plate.trim().isEmpty()) {
            throw new ValidationException("Placa do veículo obrigatória.");
        }
        Driver d = auth.registerDriver(name, email, phone, password, doc, plate, model, year, color);
        System.out.println("\nMotorista cadastrado com sucesso!");
        System.out.println("Nome: " + d.getName());
        System.out.println("Email: " + d.getEmail());
        System.out.println("Veículo: " + d.getVehicle().getModel() + " (" + d.getVehicle().getCategory() + ")");
    }

    private static void registerDelivery(MenuContext context) {
        System.out.println("=== Cadastro de Entregador ===");

        System.out.print("Nome: ");
        String name = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("CPF: ");
        String cpf = sc.nextLine();

        System.out.print("Telefone: ");
        String phone = sc.nextLine();

        System.out.print("CNH (11 dígitos): ");
        String cnh = sc.nextLine();

        System.out.print("Placa do veículo: ");
        String vehicleDoc = sc.nextLine();

        try {
            Delivery delivery = deliveryService.register(name, email, cpf, phone, cnh, vehicleDoc);

            // Gerar localização inicial aleatória (simula cidade de João Pessoa/PB)
            java.util.Random random = new java.util.Random();
            double lat = -7.0 - (random.nextDouble() * 0.3); // entre -7.0 e -7.3
            double lon = -34.8 - (random.nextDouble() * 0.2); // entre -34.8 e -35.0
            String address = "Localização inicial #" + delivery.getId().substring(0, 8);

            Location initialLocation = new Location(address, "", lat, lon);
            delivery.setCurrentLocation(initialLocation);
            deliveryRepo.update(delivery);

            System.out.println("\nEntregador cadastrado com sucesso!");
            System.out.println("Nome: " + delivery.getName());
            System.out.println("Email: " + delivery.getEmail());

            // Opção para fazer login
            System.out.print("\nDeseja fazer login agora? (s/n): ");
            String login = sc.nextLine().trim().toLowerCase();
            if (login.equals("s")) {
                DeliveryMenu deliveryMenu = new DeliveryMenu(context, delivery);
                deliveryMenu.show();
            }

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void registerRestaurant(MenuContext context) {
        System.out.println("=== Cadastro de Restaurante ===");

        System.out.print("Nome: ");
        String name = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("CNPJ (14 dígitos): ");
        String cnpj = sc.nextLine();

        System.out.print("Senha: ");
        String password = sc.nextLine();

        System.out.print("Endereço: ");
        String address = sc.nextLine();

        System.out.print("Latitude (opcional, 0 se desconhecido): ");
        double lat = 0;
        try {
            lat = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            lat = 0;
        }

        System.out.print("Longitude (opcional, 0 se desconhecido): ");
        double lon = 0;
        try {
            lon = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            lon = 0;
        }

        try {
            Location location = new Location(address, "", lat, lon);
            Restaurant restaurant = restaurantService.register(name, email, password, cnpj, location);

            System.out.println("\nRestaurante cadastrado com sucesso!");
            System.out.println("Nome: " + restaurant.getName());
            System.out.println("Status: Ativo");

            // Adicionar itens ao cardápio
            System.out.print("\nDeseja adicionar itens ao cardápio agora? (s/n): ");
            String addItems = sc.nextLine().trim().toLowerCase();
            if (addItems.equals("s")) {
                while (true) {
                    System.out.print("Nome do item (ou ENTER para finalizar): ");
                    String itemName = sc.nextLine().trim();
                    if (itemName.isEmpty())
                        break;

                    System.out.print("Descrição: ");
                    String desc = sc.nextLine().trim();

                    System.out.print("Preço: R$ ");
                    double price = Double.parseDouble(sc.nextLine().trim());

                    MenuItem item = new MenuItem(itemName, desc, price);
                    restaurant.addMenuItem(item);
                    System.out.println("Item adicionado!");
                }
                restaurantRepo.update(restaurant);
                System.out.println("Cardápio configurado com " + restaurant.getMenu().size() + " itens.");
            }

            // Opção para acessar menu do restaurante
            System.out.print("\nDeseja acessar o menu do restaurante agora? (s/n): ");
            String login = sc.nextLine().trim().toLowerCase();
            if (login.equals("s")) {
                RestaurantMenu restaurantMenu = new RestaurantMenu(context, restaurant);
                restaurantMenu.show();
            }

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Preço inválido.");
        }
    }

    private static void listUsers() {
        System.out.println("\n=== Lista de Usuários ===");
        for (User u : userRepo.findAll()) {
            System.out.println(u);
        }
        System.out.println("-------------------------");
    }

    private static void listCategories() {
        System.out.println("\n=== Categorias de Veículos Disponíveis ===");
        for (VehicleCategory c : VehicleCategory.values()) {
            System.out.println(c.name() + " - " + c.getDescription());
        }
        System.out.println("------------------------------------------");
    }

    private static void adjustDynamicFare() {
        System.out.println("\n=== Ajustar Tarifa Dinâmica ===");
        System.out.print("Digite o novo fator de tarifa dinâmica (ex: 1.5): ");
        try {
            double factor = Double.parseDouble(sc.nextLine().trim());
            pricingService.setDynamicFareFactor(factor);
            System.out.println("Fator de tarifa dinâmica ajustado para: " + factor);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Por favor, digite um número válido.");
        }
    }
}
