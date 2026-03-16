package cli;

import model.*;
import util.ValidationException;
import java.io.IOException;
import java.util.List;

/**
 * Menu específico para motoristas
 */
public class DriverMenu {
    private final MenuContext context;
    private final Driver driver;

    public DriverMenu(MenuContext context, Driver driver) {
        this.context = context;
        this.driver = driver;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Menu Motorista - " + driver.getName() + " ===");
            System.out.println("Veículo: " + driver.getVehicle().getModel() + " | Categoria: "
                    + driver.getVehicle().getCategory());
            System.out.println("1 - Visualizar Corridas Disponíveis");
            System.out.println("2 - Aceitar Corrida");
            System.out.println("3 - Recusar Corrida");
            System.out.println("4 - Visualizar Rota");
            System.out.println("5 - Adicionar Veículo");
            System.out.println("6 - Avaliar Passageiro");
            System.out.println("7 - Histórico de Corridas");
            System.out.println("8 - Gerar/Visualizar Recibo");
            System.out.println("0 - Sair");
            System.out.print("> ");

            String opt = context.getScanner().nextLine().trim();

            try {
                switch (opt) {
                    case "1":
                        listAvailableRides();
                        break;
                    case "2":
                        acceptRide();
                        break;
                    case "3":
                        refuseRide();
                        break;
                    case "4":
                        viewRoute();
                        break;
                    case "5":
                        addVehicle();
                        break;
                    case "6":
                        ratePassenger();
                        break;
                    case "7":
                        showRideHistory();
                        break;
                    case "8":
                        generateOrViewReceipt();
                        break;
                    case "0":
                        System.out.println("Saindo...");
                        return;
                    default:
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
    }

    private void listAvailableRides() {
        System.out.println("=== Visualizar Corridas Disponíveis ===");

        try {
            List<Ride> availableRides = context.getRideService().getAvailableRidesForDriver(driver.getEmail());

            if (availableRides.isEmpty()) {
                System.out.println("Nenhuma corrida disponível para sua categoria de veículo.");
                return;
            }

            System.out.println("\n=== Corridas Disponíveis ===");
            System.out.println("Sua categoria: " + driver.getVehicle().getCategory());
            System.out.println("Corridas encontradas: " + availableRides.size());
            System.out.println();

            for (int i = 0; i < availableRides.size(); i++) {
                Ride ride = availableRides.get(i);
                System.out.printf("%d. ID: %s\n", i + 1, ride.getId());
                System.out.printf("   Origem: %s\n", ride.getOrigin().getAddress());
                System.out.printf("   Destino: %s\n", ride.getDestination().getAddress());
                System.out.printf("   Categoria: %s\n", ride.getVehicleCategory());
                System.out.printf("   Tempo estimado: %d minutos\n", ride.getEstimatedTimeMinutes());
                System.out.printf("   Passageiro: %s\n", ride.getPassengerEmail());
                System.out.println("   -------------------------");
            }

        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private void acceptRide() {
        System.out.println("=== Aceitar Corrida ===");

        try {
            List<Ride> availableRides = context.getRideService().getAvailableRidesForDriver(driver.getEmail());
            if (availableRides.isEmpty()) {
                System.out.println("Nenhuma corrida disponível para aceitar.");
                return;
            }

            System.out.println("--- Selecione a corrida para aceitar ---");
            for (int i = 0; i < availableRides.size(); i++) {
                Ride ride = availableRides.get(i);
                System.out.printf("%d) %s -> %s | Categoria: %s\n",
                        i + 1,
                        ride.getOrigin().getAddress(),
                        ride.getDestination().getAddress(),
                        ride.getVehicleCategory());
            }
            System.out.print("Escolha (número) ou 0 para cancelar: ");
            int option = Integer.parseInt(context.getScanner().nextLine().trim());
            if (option == 0) {
                System.out.println("Operação cancelada.");
                return;
            }
            if (option < 1 || option > availableRides.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String rideId = availableRides.get(option - 1).getId();
            context.getRideService().acceptRide(rideId, driver.getEmail());
            System.out.println("Corrida aceita com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        } catch (IOException ioe) {
            System.out.println("Erro de I/O: " + ioe.getMessage());
        }
    }

    private void refuseRide() {
        System.out.println("=== Recusar Corrida ===");
        try {
            List<Ride> availableRides = context.getRideService().getAvailableRidesForDriver(driver.getEmail());
            if (availableRides.isEmpty()) {
                System.out.println("Nenhuma corrida disponível para recusar.");
                return;
            }

            System.out.println("--- Selecione a corrida para recusar ---");
            for (int i = 0; i < availableRides.size(); i++) {
                Ride ride = availableRides.get(i);
                System.out.printf("%d) %s -> %s | Categoria: %s\n",
                        i + 1,
                        ride.getOrigin().getAddress(),
                        ride.getDestination().getAddress(),
                        ride.getVehicleCategory());
            }
            System.out.print("Escolha (número) ou 0 para cancelar: ");
            int option = Integer.parseInt(context.getScanner().nextLine().trim());
            if (option == 0) {
                System.out.println("Operação cancelada.");
                return;
            }
            if (option < 1 || option > availableRides.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String rideId = availableRides.get(option - 1).getId();
            context.getRideService().refuseRide(rideId, driver.getEmail());
            System.out.println("Corrida recusada com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private void viewRoute() {
        try {
            List<Ride> driverRides = context.getRideRepo().findAll().stream()
                    .filter(ride -> driver.getId().equals(ride.getDriverId()))
                    .filter(ride -> ride.getStatus() == Ride.RideStatus.ACEITA
                            || ride.getStatus() == Ride.RideStatus.EM_ANDAMENTO)
                    .collect(java.util.stream.Collectors.toList());

            if (driverRides.isEmpty()) {
                System.out.println("Nenhuma corrida sua disponível para visualizar rota.");
                return;
            }

            System.out.println("--- Selecione a corrida para ver a rota ---");
            for (int i = 0; i < driverRides.size(); i++) {
                Ride ride = driverRides.get(i);
                System.out.printf("%d) %s -> %s | Status: %s\n",
                        i + 1,
                        ride.getOrigin().getAddress(),
                        ride.getDestination().getAddress(),
                        ride.getStatus().getDisplayName());
            }
            System.out.print("Escolha (número) ou 0 para cancelar: ");
            int option = Integer.parseInt(context.getScanner().nextLine().trim());
            if (option == 0) {
                System.out.println("Operação cancelada.");
                return;
            }
            if (option < 1 || option > driverRides.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String rideId = driverRides.get(option - 1).getId();
            Ride ride = context.getRideService().getRideById(rideId);
            if (ride.getDriverId() == null || !ride.getDriverId().equals(driver.getId())) {
                throw new ValidationException("Você não é o motorista designado para esta corrida.");
            }
            if (ride.getOptimizedRoute() == null || ride.getOptimizedRoute().isEmpty()) {
                try {
                    context.getRideService().generateRouteForRide(rideId);
                    ride = context.getRideService().getRideById(rideId);
                } catch (Exception e) {
                    System.out.println("A rota otimizada ainda não está disponível.");
                    return;
                }
            }
            System.out.println("--- Rota Otimizada ---");
            for (String step : ride.getOptimizedRoute()) {
                System.out.println("- " + step);
            }
            System.out.println("Tempo estimado total: " + ride.getEstimatedTimeMinutes() + " minutos");
            System.out.println("----------------------");
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private void addVehicle() throws ValidationException, IOException {
        System.out.println("=== Adicionar Veículo ===");
        System.out.print("Placa do novo veículo: ");
        String plate = context.getScanner().nextLine();
        System.out.print("Modelo do novo veículo: ");
        String model = context.getScanner().nextLine();
        System.out.print("Ano do novo veículo: ");
        int year = Integer.parseInt(context.getScanner().nextLine());
        System.out.print("Cor do novo veículo: ");
        String color = context.getScanner().nextLine();

        if (plate == null || plate.trim().isEmpty()) {
            throw new ValidationException("Placa do veículo obrigatória.");
        }

        Driver d = context.getAuth().addVehicleToDriver(driver.getEmail(), plate, model, year, color);
        System.out.println("Novo veículo adicionado: " + d.getVehicle());
    }

    private void ratePassenger() throws ValidationException, IOException {
        SharedMenus.rateRide(context, driver);
    }

    private void showRideHistory() {
        SharedMenus.showRideHistory(context, driver);
    }

    private void generateOrViewReceipt() throws ValidationException {
        SharedMenus.generateOrViewReceipt(context, driver);
    }
}
