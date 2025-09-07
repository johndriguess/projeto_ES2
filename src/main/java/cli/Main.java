package cli;

import repo.UserRepository;
import service.AuthService;
import model.User;
import model.Driver;
import model.Passenger;
import util.ValidationException;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final String DB = "users.db";

    public static void main(String[] args) {
        UserRepository repo = new UserRepository(DB);
        AuthService auth = new AuthService(repo);
        Scanner sc = new Scanner(System.in);
        System.out.println("=== UberPB - Cadastro (RF01) ===");

        while (true) {
            System.out.println("\nEscolha uma opção:");
            System.out.println("1 - Cadastrar Passageiro");
            System.out.println("2 - Cadastrar Motorista");
            System.out.println("3 - Listar usuários");
            System.out.println("0 - Sair");
            System.out.print("> ");
            String opt = sc.nextLine().trim();
            try {
                if (opt.equals("1")) {
                    System.out.print("Nome: "); String name = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Telefone: "); String phone = sc.nextLine();
                    Passenger p = auth.registerPassenger(name, email, phone);
                    System.out.println("Passageiro cadastrado: " + p);
                } else if (opt.equals("2")) {
                    System.out.print("Nome: "); String name = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Telefone: "); String phone = sc.nextLine();
                    System.out.print("Documento (CNH): "); String doc = sc.nextLine();
                    System.out.print("Placa do veículo: "); String plate = sc.nextLine();
                    System.out.print("Modelo do veículo: "); String model = sc.nextLine();
                    Driver d = auth.registerDriver(name, email, phone, doc, plate, model);
                    System.out.println("Motorista cadastrado: " + d);
                } else if (opt.equals("3")) {
                    for (User u : repo.findAll()) {
                        System.out.println(u);
                    }
                } else if (opt.equals("0")) {
                    System.out.println("Saindo...");
                    break;
                } else {
                    System.out.println("Opção inválida.");
                }
            } catch (ValidationException ve) {
                System.out.println("Erro de validação: " + ve.getMessage());
            } catch (IOException ioe) {
                System.out.println("Erro de I/O: " + ioe.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
        sc.close();
    }
}
