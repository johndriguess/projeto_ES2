package cli;

import model.*;
import util.ValidationException;
import java.io.IOException;
import java.util.List;

/**
 * Menu específico para passageiros
 */
public class PassengerMenu {
    private final MenuContext context;
    private final Passenger passenger;

    public PassengerMenu(MenuContext context, Passenger passenger) {
        this.context = context;
        this.passenger = passenger;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Menu Passageiro - " + passenger.getName() + " ===");
            System.out.println("1 - Solicitar Corrida");
            System.out.println("2 - Calcular Preços de Corrida");
            System.out.println("3 - Acompanhar Corrida");
            System.out.println("4 - Pagar Corrida");
            System.out.println("5 - Gerar/Visualizar Recibo");
            System.out.println("6 - Avaliar Corrida");
            System.out.println("7 - Histórico de Corridas");
            System.out.println("8 - Listar Minhas Corridas");
            System.out.println("9 - Listar Restaurantes Disponíveis");
            System.out.println("10 - Ver Cardápio de Restaurante");
            System.out.println("11 - Fazer Pedido Imediato");
            System.out.println("12 - Fazer Pedido Agendado");
            System.out.println("13 - Consultar Status do Pedido");
            System.out.println("14 - Avaliar Entregador/Restaurante");
            System.out.println("0 - Sair");
            System.out.print("> ");

            String opt = context.getScanner().nextLine().trim();

            try {
                switch (opt) {
                    case "1":
                        requestRide();
                        break;
                    case "2":
                        calculatePricing();
                        break;
                    case "3":
                        trackRide();
                        break;
                    case "4":
                        payRide();
                        break;
                    case "5":
                        generateOrViewReceipt();
                        break;
                    case "6":
                        rateRide();
                        break;
                    case "7":
                        showRideHistory();
                        break;
                    case "8":
                        listMyRides();
                        break;
                    case "9":
                        listAvailableRestaurants();
                        break;
                    case "10":
                        viewRestaurantDetails();
                        break;
                    case "11":
                        SharedMenus.createImmediateOrder(context);
                        break;
                    case "12":
                        SharedMenus.createScheduledOrder(context);
                        break;
                    case "13":
                        viewOrderStatus();
                        break;
                    case "14":
                        rateOrderParticipants();
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

    private void requestRide() throws ValidationException, IOException {
        System.out.println("=== Solicitar Corrida ===");
        System.out.print("Endereço de origem: ");
        String origin = context.getScanner().nextLine();
        System.out.print("Endereço de destino: ");
        String destination = context.getScanner().nextLine();

        List<PricingInfo> pricingList = context.getRideService().calculateAllPricing(origin, destination);
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
        int optionIndex = Integer.parseInt(context.getScanner().nextLine().trim()) - 1;

        if (optionIndex >= 0 && optionIndex < pricingList.size()) {
            System.out.println("\n--- Formas de Pagamento ---");
            for (PaymentMethod pm : PaymentMethod.values()) {
                System.out.println((pm.ordinal() + 1) + " - " + pm.getDisplayName());
            }
            System.out.print("Escolha uma forma de pagamento: ");
            int paymentOption = Integer.parseInt(context.getScanner().nextLine().trim()) - 1;
            PaymentMethod selectedPaymentMethod = PaymentMethod.values()[paymentOption];

            PricingInfo selectedPricing = pricingList.get(optionIndex);
            Ride ride = context.getRideService().createRideRequest(passenger.getEmail(), origin, destination,
                    selectedPricing.getCategory(), selectedPaymentMethod);

            System.out.println("\n=== Corrida Solicitada com Sucesso! ===");
            System.out.println("Categoria escolhida: " + selectedPricing.getCategory());
            System.out.println("Preço estimado: " + selectedPricing.getFormattedPrice());
            System.out.println("Tempo estimado: " + selectedPricing.getFormattedTime());
            System.out.println("Forma de Pagamento: " + selectedPaymentMethod.getDisplayName());
            System.out.println("Status: " + ride.getStatus().getDisplayName());
            if (ride.getDriverId() != null) {
                Driver assignedDriver = (Driver) context.getUserRepo().findById(ride.getDriverId());
                if (assignedDriver != null) {
                    double driverRating = assignedDriver.getAverageRating();
                    System.out.println("Motorista atribuído: " + assignedDriver.getName());
                    System.out.printf("Nota do motorista: %.2f\n", driverRating);
                }
            }
        } else {
            System.out.println("Opção inválida.");
        }
    }

    private void calculatePricing() {
        System.out.println("=== Calcular Preços ===");
        System.out.print("Endereço de origem: ");
        String origin = context.getScanner().nextLine();
        System.out.print("Endereço de destino: ");
        String destination = context.getScanner().nextLine();

        try {
            List<PricingInfo> pricingList = context.getRideService().calculateAllPricing(origin, destination);
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

    private void trackRide() {
        try {
            List<Ride> rides = context.getRideService().getRidesByPassenger(passenger.getEmail());
            rides = rides.stream()
                    .filter(ride -> ride.getStatus() != Ride.RideStatus.FINALIZADA
                            && ride.getStatus() != Ride.RideStatus.CANCELADA)
                    .collect(java.util.stream.Collectors.toList());

            if (rides.isEmpty()) {
                System.out.println("Você não possui corridas ativas para acompanhar.");
                return;
            }

            System.out.println("--- Selecione a corrida para acompanhar ---");
            for (int i = 0; i < rides.size(); i++) {
                Ride ride = rides.get(i);
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
            if (option < 1 || option > rides.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String rideId = rides.get(option - 1).getId();
            Ride ride = context.getRideService().getRideById(rideId);
            System.out.println("--- Acompanhamento de Corrida ---");
            System.out.println("Status: " + ride.getStatus().getDisplayName());
            if (ride.getDriverId() != null) {
                Driver assignedDriver = (Driver) context.getUserRepo().findById(ride.getDriverId());
                if (assignedDriver != null) {
                    System.out.println("Motorista: " + assignedDriver.getName());
                    System.out.printf("Nota do motorista: %.2f\n", assignedDriver.getAverageRating());
                }
            }
            if (ride.getStatus() == Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA) {
                System.out.println("A corrida foi atribuída e está aguardando o aceite do motorista.");
            }
            if (ride.getStatus() == Ride.RideStatus.ACEITA || ride.getStatus() == Ride.RideStatus.EM_ANDAMENTO) {
                if (ride.getDriverCurrentLocation() != null) {
                    System.out
                            .println("Localização atual do motorista: " + ride.getDriverCurrentLocation().getAddress());
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

    private void payRide() {
        System.out.println("=== Pagar Corrida ===");

        try {
            List<Ride> rides = context.getRideService().getRidesByPassenger(passenger.getEmail());
            rides = rides.stream()
                    .filter(ride -> ride.getStatus() == Ride.RideStatus.ACEITA)
                    .collect(java.util.stream.Collectors.toList());

            if (rides.isEmpty()) {
                System.out.println("Nenhuma corrida apta para pagamento.");
                return;
            }

            System.out.println("--- Selecione a corrida para pagamento ---");
            for (int i = 0; i < rides.size(); i++) {
                Ride ride = rides.get(i);
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
            if (option < 1 || option > rides.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String rideId = rides.get(option - 1).getId();
            boolean paymentSuccess = context.getRideService().processRidePayment(rideId);

            if (paymentSuccess) {
                System.out.println("\nPagamento realizado com sucesso!");
                System.out.println("A corrida pode ser finalizada agora.");
            } else {
                System.out.println("\nFalha no pagamento. Tente novamente.");
            }
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private void generateOrViewReceipt() throws ValidationException {
        SharedMenus.generateOrViewReceipt(context, passenger);
    }

    private void rateRide() throws ValidationException, IOException {
        SharedMenus.rateRide(context, passenger);
    }

    private void showRideHistory() {
        SharedMenus.showRideHistory(context, passenger);
    }

    private void listMyRides() {
        try {
            List<Ride> rides = (List<Ride>) context.getRideService().getRidesByPassenger(passenger.getEmail());
            if (rides.isEmpty()) {
                System.out.println("Nenhuma corrida encontrada.");
                return;
            }
            System.out.println("--- Suas Corridas ---");
            for (Ride ride : rides)
                System.out.println(ride);
            System.out.println("--------------------");
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        }
    }

    private void listAvailableRestaurants() {
        SharedMenus.listAvailableRestaurants(context);
    }

    private void viewRestaurantDetails() {
        SharedMenus.viewRestaurantDetails(context);
    }

    private void viewOrderStatus() {
        SharedMenus.viewOrderStatus(context, passenger.getEmail());
    }

    private void rateOrderParticipants() {
        SharedMenus.customerRateOrder(context, passenger.getEmail());
    }
}
