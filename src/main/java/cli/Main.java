package cli;

import repo.UserRepository;
import repo.VehicleRepository;
import service.AuthService;
import model.User;
import model.Driver;
import model.Passenger;
import util.ValidationException;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static final String USER_DB = "users.db";
    private static final String VEHICLE_DB = "vehicles.db";
    private static UserRepository userRepo;
    private static VehicleRepository vehicleRepo;
    private static AuthService auth;
    private static Scanner sc;

    public static void main(String[] args) {
        userRepo = new UserRepository(USER_DB);
        vehicleRepo = new VehicleRepository(VEHICLE_DB);
        auth = new AuthService(userRepo, vehicleRepo);
        sc = new Scanner(System.in);

        System.out.println("=== UberPB - Cadastro e Veículos ===");

        while (true) {
            System.out.println("\nEscolha uma opção:");
            System.out.println("1 - Cadastrar Passageiro");
            System.out.println("2 - Cadastrar Motorista");
            System.out.println("3 - Adicionar Veículo a Motorista");
            System.out.println("4 - Listar usuários");
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
                        listUsers();
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
                System.out.println("Erro: A entrada para o ano do veículo deve ser um número inteiro.");
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
        Passenger p = auth.registerPassenger(name, email, phone);
        System.out.println("Passageiro cadastrado: " + p);
    }

    private static void registerDriver() throws ValidationException, IOException {
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Telefone: ");
        String phone = sc.nextLine();
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

        Driver d = auth.registerDriver(name, email, phone, doc, plate, model, year, color);
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

    private static void listUsers() {
        System.out.println("--- Lista de Usuários ---");
        for (User u : userRepo.findAll()) {
            System.out.println(u);
        }
        System.out.println("-------------------------");
    }
}