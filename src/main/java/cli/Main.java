package cli;

import repo.UserRepository;
import repo.VehicleRepository;
import repo.RideRepository;
import service.AuthService;
import service.RideService;
import service.PricingService;
import model.User;
import model.Driver;
import model.Passenger;
import model.VehicleCategory;
import model.Ride;
import model.PricingInfo;
import util.ValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;

public class Main {
    private static final String USER_DB = "users.db";
    private static final String VEHICLE_DB = "vehicles.db";
    private static final String RIDE_DB = "rides.db";
    private static UserRepository userRepo;
    private static VehicleRepository vehicleRepo;
    private static RideRepository rideRepo;
    private static AuthService auth;
    private static RideService rideService;
    private static PricingService pricingService;
    private static Scanner sc;

    public static void main(String[] args) {
        userRepo = new UserRepository();
        vehicleRepo = new VehicleRepository(VEHICLE_DB);
        rideRepo = new RideRepository(RIDE_DB);
        auth = new AuthService(userRepo, vehicleRepo);
        pricingService = new PricingService();
        rideService = new RideService(rideRepo, userRepo, pricingService);
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
            System.out.println("9 - Calcular preços (RF05 + RF14)");
            System.out.println("10 - Ver Corridas Disponíveis (RF08)");
            System.out.println("11 - Acompanhar Corrida (RF10)");
            System.out.println("12 - Visualizar Rota (RF12)");
            System.out.println("13 - Ajustar Tarifa Dinâmica (RF14)");
            System.out.println("14 - Gerar / Enviar / Visualizar Recibo (RF15)");
            System.out.println("0 - Sair");
            System.out.print("> ");
            String opt = sc.nextLine().trim();

            try {
                switch (opt) {
                    case "1": registerPassenger(); break;
                    case "2": registerDriver(); break;
                    case "3": addVehicleToDriver(); break;
                    case "4": loginUser(); break;
                    case "5": listUsers(); break;
                    case "6": listCategories(); break;
                    case "7": requestRideWithPricing(); break;
                    case "8": listMyRides(); break;
                    case "9": calculatePricing(); break;
                    case "10": viewAvailableRides(); break;
                    case "11": trackRide(); break;
                    case "12": viewRoute(); break;
                    case "13": adjustDynamicFare(); break;
                    case "14": generateOrViewReceipt(); break;
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

    // === RF15: Recibo Eletrônico ===
    private static void generateOrViewReceipt() {
        System.out.println("=== Gerar / Enviar / Visualizar Recibo (RF15) ===");
        System.out.print("Digite o ID da corrida: ");
        String rideId = sc.nextLine().trim();
        System.out.print("Forma de pagamento (ou deixe em branco para 'Não informado'): ");
        String paymentMethod = sc.nextLine().trim();
        if (paymentMethod.isEmpty()) paymentMethod = "Não informado";

        try {
            // Emite o recibo detalhado
            rideService.emitReceiptForRide(rideId, paymentMethod);

            // Pasta onde os recibos são salvos
            String receiptsFolder = "receipts";
            File folder = new File(receiptsFolder);
            if (!folder.exists()) {
                System.out.println("Nenhum recibo salvo encontrado.");
                return;
            }

            // listar arquivos relacionados à corrida
            File[] files = folder.listFiles((dir, name) -> name.contains(rideId));
            if (files == null || files.length == 0) {
                System.out.println("Nenhum arquivo de recibo encontrado para a corrida " + rideId);
                return;
            }

            System.out.println("Recibos encontrados:");
            for (int i = 0; i < files.length; i++) {
                System.out.printf("%d) %s\n", i + 1, files[i].getName());
            }
            System.out.print("Escolha número para visualizar (ou ENTER para voltar): ");
            String choice = sc.nextLine().trim();
            if (choice.isEmpty()) return;
            int idx = Integer.parseInt(choice) - 1;
            if (idx < 0 || idx >= files.length) {
                System.out.println("Opção inválida.");
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(files[idx].getPath())));
            System.out.println("\n--- Conteúdo do Recibo ---\n");
            System.out.println(content);
            System.out.println("\n--- Fim do Recibo ---\n");
        } catch (IOException ioe) {
            System.out.println("Erro de I/O ao gerar/visualizar recibo: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }


    private static void adjustDynamicFare() {
        System.out.print("Digite o novo fator de tarifa dinâmica (ex: 1.5): ");
        try {
            double factor = Double.parseDouble(sc.nextLine().trim());
            pricingService.setDynamicFareFactor(factor);
            System.out.println("Fator de tarifa dinâmica ajustado para: " + factor);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Por favor, digite um número válido.");
        }
    }

    private static void viewRoute() {
        System.out.print("Digite o ID da corrida: ");
        String rideId = sc.nextLine().trim();
        System.out.print("Digite seu e-mail de motorista: ");
        String driverEmail = sc.nextLine().trim();
        try {
            User user = userRepo.findByEmail(driverEmail);
            if (user == null || !(user instanceof Driver)) {
                throw new ValidationException("Você precisa ser um motorista para visualizar uma rota.");
            }
            Ride ride = rideService.getRideById(rideId);
            if (ride.getDriverId() == null || !ride.getDriverId().equals(user.getId())) {
                throw new ValidationException("Você não é o motorista designado para esta corrida.");
            }
            if (ride.getOptimizedRoute() == null || ride.getOptimizedRoute().isEmpty()) {
                System.out.println("A rota otimizada ainda não está disponível.");
                return;
            }
            System.out.println("--- Rota Otimizada ---");
            for (String step : ride.getOptimizedRoute()) {
                System.out.println("- " + step);
            }
            System.out.println("----------------------");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private static void trackRide() {
        System.out.print("Digite o ID da corrida para acompanhar: ");
        String rideId = sc.nextLine().trim();
        try {
            Ride ride = rideService.getRideById(rideId);
            System.out.println("--- Acompanhamento de Corrida ---");
            System.out.println("ID da corrida: " + ride.getId());
            System.out.println("Status: " + ride.getStatus().getDisplayName());
            if (ride.getStatus() == Ride.RideStatus.ACEITA || ride.getStatus() == Ride.RideStatus.EM_ANDAMENTO) {
                if (ride.getDriverCurrentLocation() != null) {
                    System.out.println("Localização atual do motorista: " + ride.getDriverCurrentLocation().getAddress());
                } else {
                    System.out.println("Localização do motorista ainda não disponível.");
                }
            } else {
                System.out.println("Não é possível acompanhar uma corrida com este status.");
            }
            System.out.println("------------------------------");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
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
            List<Ride> rides = (List<Ride>) rideService.getRidesByPassenger(email);
            if (rides.isEmpty()) {
                System.out.println("Nenhuma corrida encontrada para este passageiro.");
                return;
            }
            System.out.println("--- Suas Corridas ---");
            for (Ride ride : rides) System.out.println(ride);
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
            List<PricingInfo> pricingList = rideService.calculateAllPricing(origin, destination);
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
            List<PricingInfo> pricingList = rideService.calculateAllPricing(origin, destination);
            System.out.println("\n=== Estimativas de Corrida ===");
            System.out.println("Origem: " + origin);
            System.out.println("Destino: " + destination);
            System.out.println("Distância estimada: " + pricingList.get(0).getFormattedDistance());
            System.out.println("Tempo estimado: " + pricingList.get(0).getFormattedTime());
            System.out.println();
            for (int i = 0; i < pricingList.size(); i++) {
                PricingInfo pricing = pricingList.get(i);
                System.out.printf("%d. %s - %s (%s)\n",
                        i + 1, pricing.getCategory(), pricing.getFormattedPrice(), pricing.getFormattedTime());
            }
            System.out.print("Escolha uma opção (1-" + pricingList.size() + "): ");
            int optionIndex = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (optionIndex >= 0 && optionIndex < pricingList.size()) {
                PricingInfo selectedPricing = pricingList.get(optionIndex);
                Ride ride = rideService.createRideRequest(email, origin, destination, selectedPricing.getCategory());
                System.out.println("\n=== Corrida Solicitada com Sucesso! ===");
                System.out.println("ID da corrida: " + ride.getId());
                System.out.println("Categoria escolhida: " + selectedPricing.getCategory());
                System.out.println("Preço estimado: " + selectedPricing.getFormattedPrice());
                System.out.println("Tempo estimado: " + selectedPricing.getFormattedTime());
                System.out.println("Status: " + ride.getStatus().getDisplayName());
            } else {
                System.out.println("Opção inválida.");
            }
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private static void viewAvailableRides() throws ValidationException, IOException {
        System.out.print("Email do motorista: ");
        String email = sc.nextLine();
        User user = userRepo.findByEmail(email);
        if (user == null || !(user instanceof Driver)) {
            System.out.println("Apenas motoristas podem ver as corridas disponíveis.");
            return;
        }
        List<Ride> pendingRides = rideService.getPendingRidesForDriver(email);
        if (pendingRides.isEmpty()) {
            System.out.println("Nenhuma corrida disponível para você no momento.");
            return;
        }
        System.out.println("--- Corridas Disponíveis ---");
        for (Ride ride : pendingRides) System.out.println(ride);
        System.out.println("--------------------------");
        System.out.print("Digite o ID da corrida que deseja aceitar ou 'cancelar' para voltar: ");
        String rideId = sc.nextLine();
        if (rideId.equalsIgnoreCase("cancelar")) return;
        System.out.print("Você deseja (A)ceitar ou (R)ecusar esta corrida? ");
        String choice = sc.nextLine();
        if (choice.equalsIgnoreCase("A")) {
            rideService.acceptRide(rideId, email);
        } else if (choice.equalsIgnoreCase("R")) {
            rideService.refuseRide(rideId, email);
        } else {
            System.out.println("Opção inválida.");
        }
    }
}
