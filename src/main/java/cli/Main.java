package cli;

import repo.UserRepository;
import repo.VehicleRepository;
import repo.RideRepository;
import service.AuthService;
import service.RideService;
import model.User;
import model.Driver;
import model.Passenger;
import model.VehicleCategory;
import model.Ride;
import model.PricingInfo;
import util.ValidationException;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static final String USER_DB = "users.db";
    private static final String VEHICLE_DB = "vehicles.db";
    private static final String RIDE_DB = "rides.db";
    private static UserRepository userRepo;
    private static VehicleRepository vehicleRepo;
    private static RideRepository rideRepo;
    private static AuthService auth;
    private static RideService rideService;
    private static Scanner sc;

    public static void main(String[] args) {
        userRepo = new UserRepository(USER_DB);
        vehicleRepo = new VehicleRepository(VEHICLE_DB);
        rideRepo = new RideRepository(RIDE_DB);
        auth = new AuthService(userRepo, vehicleRepo);
        rideService = new RideService(rideRepo, userRepo);
        sc = new Scanner(System.in);
        System.out.println("=== UberPB ===");
        while (true) {
            System.out.println("\nEscolha uma opção:");
            System.out.println("1 - Cadastrar Passageiro (RF01)");
            System.out.println("2 - Cadastrar Motorista (RF01)");
            System.out.println("3 - Adicionar Veículo a Motorista");
            System.out.println("4 - Fazer Login");
            System.out.println("5 - Listar usuários");
            System.out.println("6 - Listar categorias de veículos (RF06)");
            System.out.println("7 - Solicitar Corrida (RF04 + RF05)");
            System.out.println("8 - Listar minhas corridas");
            System.out.println("9 - Calcular preços (RF05)");

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
                        addVehicleToDriver();
                        break;
                    case "4":
                        loginUser();
                        break;
                    case "5":
                        listUsers();
                        break;
                    case "6":
                        listCategories();
                        break;
                    case "7":
                        requestRideWithPricing();
                        break;
                    case "8":
                        listMyRides();
                        break;
                    case "9":
                        calculatePricing();
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
            }
        }
    }

    private static void registerPassenger() throws ValidationException, IOException {
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Telefone: ");
        String phone = sc.nextLine();
        System.out.print("Senha: ");
        String password = sc.nextLine();
        Passenger p = auth.registerPassenger(name, email, phone, password);
        System.out.println("Passageiro cadastrado: " + p);
    }

    private static void registerDriver() throws ValidationException, IOException {
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Telefone: ");
        String phone = sc.nextLine();
        System.out.print("Senha: ");
        String password = sc.nextLine();
        System.out.print("Documento (CNH): ");
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
        System.out.println("Motorista cadastrado: " + d);
    }

    private static void addVehicleToDriver() throws ValidationException, IOException {
        System.out.print("Email do motorista: ");
        String email = sc.nextLine();
        System.out.print("Placa do novo veículo: ");
        String plate = sc.nextLine();
        System.out.print("Modelo do novo veículo: ");
        String model = sc.nextLine();
        System.out.print("Ano do novo veículo: ");
        int year = Integer.parseInt(sc.nextLine());
        System.out.print("Cor do novo veículo: ");
        String color = sc.nextLine();

        if (plate == null || plate.trim().isEmpty()) {
            throw new ValidationException("Placa do veículo obrigatória.");
        }

        Driver d = auth.addVehicleToDriver(email, plate, model, year, color);
        System.out.println("Novo veículo adicionado ao motorista: " + d);
    }
    
    private static void loginUser() {
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Senha: ");
        String password = sc.nextLine();
        
        try {
            User loggedInUser = auth.login(email, password);
            System.out.println("Login bem-sucedido! Bem-vindo, " + loggedInUser.getName() + " (" + loggedInUser.getRole() + ").");
        } catch (ValidationException ve) {
            System.out.println("Erro de login: " + ve.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado durante o login: " + e.getMessage());
        }
    }

    private static void listUsers() {
        System.out.println("--- Lista de Usuários ---");
        for (User u : userRepo.findAll()) {
            System.out.println(u);
        }
        System.out.println("-------------------------");
    }

    private static void listCategories() {
        System.out.println("--- Categorias de Veículos Disponíveis ---");
        for (VehicleCategory c : VehicleCategory.values()) {
            System.out.println(c.name() + " - " + c.getDescription());
        }
        System.out.println("------------------------------------------");
    }


    private static void listMyRides() {
        System.out.print("Email do passageiro: ");
        String email = sc.nextLine();
        
        try {
            java.util.Collection<Ride> rides = rideService.getRidesByPassenger(email);
            
            if (rides.isEmpty()) {
                System.out.println("Nenhuma corrida encontrada para este passageiro.");
                return;
            }
            
            System.out.println("--- Suas Corridas ---");
            for (Ride ride : rides) {
                System.out.println(ride);
            }
            System.out.println("--------------------");
            
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private static void calculatePricing() {
        System.out.println("=== Calcular Preços (RF05) ===");
        
        System.out.print("Endereço de origem: ");
        String origin = sc.nextLine();
        
        System.out.print("Endereço de destino: ");
        String destination = sc.nextLine();
        
        try {
            java.util.List<PricingInfo> pricingList = rideService.calculateAllPricing(origin, destination);
            
            System.out.println("\n=== Estimativas de Corrida ===");
            System.out.println("Origem: " + origin);
            System.out.println("Destino: " + destination);
            System.out.println("Distância estimada: " + pricingList.get(0).getFormattedDistance());
            System.out.println("Tempo estimado: " + pricingList.get(0).getFormattedTime());
            System.out.println();
            
            System.out.println("Opções disponíveis:");
            for (int i = 0; i < pricingList.size(); i++) {
                PricingInfo pricing = pricingList.get(i);
                System.out.printf("%d. %s - %s (%s)\n", 
                    i + 1, pricing.getCategory(), pricing.getFormattedPrice(), pricing.getFormattedTime());
            }
            System.out.println("=============================");
            
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private static void requestRideWithPricing() throws ValidationException, IOException {
        System.out.println("=== Solicitar Corrida (RF04 + RF05) ===");
        
        System.out.print("Email do passageiro: ");
        String email = sc.nextLine();
        
        System.out.print("Endereço de origem: ");
        String origin = sc.nextLine();
        
        System.out.print("Endereço de destino: ");
        String destination = sc.nextLine();
        
        try {
            // Calcular preços para todas as categorias
            java.util.List<PricingInfo> pricingList = rideService.calculateAllPricing(origin, destination);
            
            System.out.println("\n=== Estimativas de Corrida ===");
            System.out.println("Origem: " + origin);
            System.out.println("Destino: " + destination);
            System.out.println("Distância estimada: " + pricingList.get(0).getFormattedDistance());
            System.out.println("Tempo estimado: " + pricingList.get(0).getFormattedTime());
            System.out.println();
            
            System.out.println("Opções disponíveis:");
            for (int i = 0; i < pricingList.size(); i++) {
                PricingInfo pricing = pricingList.get(i);
                System.out.printf("%d. %s - %s (%s)\n", 
                    i + 1, pricing.getCategory(), pricing.getFormattedPrice(), pricing.getFormattedTime());
            }
            System.out.println();
            
            System.out.print("Escolha uma opção (1-" + pricingList.size() + "): ");
            String choice = sc.nextLine().trim();
            
            try {
                int optionIndex = Integer.parseInt(choice) - 1;
                if (optionIndex >= 0 && optionIndex < pricingList.size()) {
                    PricingInfo selectedPricing = pricingList.get(optionIndex);
                    
                    // Criar a corrida
                    Ride ride = rideService.createRideRequest(email, origin, destination);
                    
                    // Definir a categoria escolhida
                    rideService.setVehicleCategory(ride.getId(), selectedPricing.getCategory());
                    
                    System.out.println("\n=== Corrida Solicitada com Sucesso! ===");
                    System.out.println("ID da corrida: " + ride.getId());
                    System.out.println("Categoria escolhida: " + selectedPricing.getCategory());
                    System.out.println("Preço estimado: " + selectedPricing.getFormattedPrice());
                    System.out.println("Tempo estimado: " + selectedPricing.getFormattedTime());
                    System.out.println("Status: " + ride.getStatus().getDisplayName());
                    System.out.println("=====================================");
                    
                } else {
                    System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
            }
            
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }
}
