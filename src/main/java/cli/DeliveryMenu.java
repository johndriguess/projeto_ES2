package cli;

import model.*;
import util.DistanceCalculator;
import util.ValidationException;

import java.util.List;

/**
 * Menu específico para entregadores
 */
public class DeliveryMenu {
    private final MenuContext context;
    private final Delivery delivery;

    public DeliveryMenu(MenuContext context, Delivery delivery) {
        this.context = context;
        this.delivery = delivery;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Menu Entregador - " + delivery.getName() + " ===");
            double avg = context.getAvaliacaoService().getAverageRatingForDelivery(delivery.getId());
            int count = context.getAvaliacaoService().getTotalRatingsForDelivery(delivery.getId());
            System.out.printf("Avaliação média: %.2f (%d avaliações)\n", avg, count);
            System.out.println("Status: " + (delivery.isActive() ? "Ativo" : "Inativo"));
            if (delivery.getCurrentLocation() != null) {
                System.out.println("Localização: " + delivery.getCurrentLocation().getAddress());
            }
            System.out.println("1 - Atualizar Localização");
            System.out.println("2 - Ver Notificações");
            System.out.println("3 - Alternar Disponibilidade");
            System.out.println("4 - Ver Minhas Entregas / Aceitar ou Rejeitar Pedido");
            System.out.println("5 - Avaliar Cliente/Restaurante");
            System.out.println("6 - Ver Rota da Entrega");
            System.out.println("0 - Sair");
            System.out.print("> ");

            String opt = context.getScanner().nextLine().trim();

            try {
                switch (opt) {
                    case "1":
                        updateLocation();
                        break;
                    case "2":
                        viewNotifications();
                        break;
                    case "3":
                        toggleAvailability();
                        break;
                    case "4":
                        viewMyDeliveries();
                        break;
                    case "5":
                        rateOrderParticipants();
                        break;
                    case "6":
                        viewDeliveryRoute();
                        break;
                    case "0":
                        System.out.println("Saindo...");
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (ValidationException ve) {
                System.out.println("Erro de validação: " + ve.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    private void updateLocation() {
        System.out.println("=== Atualizar Localização ===");

        System.out.print("Novo endereço: ");
        String address = context.getScanner().nextLine().trim();

        System.out.print("Latitude: ");
        double lat = Double.parseDouble(context.getScanner().nextLine().trim());

        System.out.print("Longitude: ");
        double lon = Double.parseDouble(context.getScanner().nextLine().trim());

        Location newLocation = new Location(address, "", lat, lon);
        delivery.setCurrentLocation(newLocation);

        // Salvar alteração no repositório
        context.getDeliveryRepo().update(delivery);

        System.out.println("\nLocalização atualizada com sucesso!");
        System.out.println("Nova localização: " + newLocation.getAddress());
    }

    private void viewNotifications() {
        SharedMenus.viewNotifications(context, delivery.getId());
    }

    private void toggleAvailability() {
        if (delivery.isActive()) {
            delivery.deactivate();
            System.out.println("Status atualizado para: Inativo");
        } else {
            delivery.activate();
            System.out.println("Status atualizado para: Ativo");
        }
        context.getDeliveryRepo().update(delivery);
    }

    private void viewMyDeliveries() {
        System.out.println("\n=== Minhas Entregas ===");

        // Buscar todos os pedidos atribuídos a este entregador
        var allOrders = context.getOrderRepo().findAll();
        boolean found = false;

        for (Order order : allOrders) {
            if (order.getAssignedDeliveryId() != null &&
                    order.getAssignedDeliveryId().equals(delivery.getId())) {
                found = true;
                System.out.println("\nPedido ID: " + order.getId());
                System.out.println("Restaurante ID: " + order.getRestaurantId());
                System.out.println("Status: " + order.getStatus());
                System.out.println("Total: R$ " + String.format("%.2f", order.getTotal()));
                System.out.println("Tipo: " + order.getOrderType().getDisplayName());
                System.out.println("------------------------");
            }
        }

        if (!found) {
            System.out.println("Você não tem entregas no momento.");
            return;
        }

        var pendingOrders = allOrders.stream()
                .filter(order -> delivery.getId().equals(order.getAssignedDeliveryId()))
                .filter(Order::isAwaitingDeliveryAcceptance)
                .collect(java.util.stream.Collectors.toList());

        if (pendingOrders.isEmpty()) {
            return;
        }

        System.out.println("\nPedidos aguardando sua decisão:");
        for (int i = 0; i < pendingOrders.size(); i++) {
            Order order = pendingOrders.get(i);
            System.out.printf("%d) Pedido %s - Total: R$ %.2f\n", i + 1, order.getId(), order.getTotal());
        }

        System.out.print("\nDeseja aceitar ou rejeitar um pedido? (a/r/n): ");
        String action = context.getScanner().nextLine().trim().toLowerCase();
        if (!"a".equals(action) && !"r".equals(action)) {
            return;
        }

        System.out.print("Escolha o número do pedido: ");
        int choice;
        try {
            choice = Integer.parseInt(context.getScanner().nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (choice < 1 || choice > pendingOrders.size()) {
            System.out.println("Opção inválida.");
            return;
        }

        String orderId = pendingOrders.get(choice - 1).getId();
        if ("a".equals(action)) {
            context.getOrderService().acceptOrderByDelivery(orderId, delivery.getId());
            System.out.println("Pedido aceito com sucesso.");
        } else {
            context.getOrderService().rejectOrderByDelivery(orderId, delivery.getId());
            System.out.println("Pedido rejeitado com sucesso.");
        }
    }

    private void rateOrderParticipants() {
        SharedMenus.deliveryRateOrder(context, delivery);
    }

    private void viewDeliveryRoute() {
        System.out.println("\n=== Ver Rota da Entrega ===");

        if (delivery.getCurrentLocation() == null) {
            System.out.println("Atualize sua localização antes de visualizar a rota.");
            return;
        }

        List<Order> assignedOrders = context.getOrderRepo().findAll().stream()
                .filter(order -> delivery.getId().equals(order.getAssignedDeliveryId()))
                .filter(order -> !order.isRejected() && !order.isDelivered())
                .collect(java.util.stream.Collectors.toList());

        if (assignedOrders.isEmpty()) {
            System.out.println("Você não possui pedidos ativos para rota.");
            return;
        }

        for (int i = 0; i < assignedOrders.size(); i++) {
            Order order = assignedOrders.get(i);
            System.out.printf("%d) Pedido %s - Status: %s\n", i + 1, order.getId(), order.getStatus());
        }

        System.out.print("Escolha o número do pedido (0 para cancelar): ");
        int choice;
        try {
            choice = Integer.parseInt(context.getScanner().nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (choice == 0) {
            return;
        }

        if (choice < 1 || choice > assignedOrders.size()) {
            System.out.println("Opção inválida.");
            return;
        }

        Order selectedOrder = assignedOrders.get(choice - 1);
        Restaurant restaurant = context.getRestaurantRepo().findById(selectedOrder.getRestaurantId()).orElse(null);

        if (restaurant == null || restaurant.getLocation() == null) {
            System.out.println("Não foi possível obter a localização do restaurante para este pedido.");
            return;
        }

        String from = delivery.getCurrentLocation().getAddress();
        String to = restaurant.getLocation().getAddress();
        double distanceKm = DistanceCalculator.calculateDistance(from, to);
        int etaMinutes = DistanceCalculator.calculateEstimatedTime(from, to);

        System.out.println("\n--- Rota Atual ---");
        System.out.println("Trecho: Você -> Restaurante");
        System.out.println("Origem: " + from);
        System.out.println("Destino: " + to);
        System.out.printf("Distância estimada: %.1f km\n", distanceKm);
        System.out.println("Tempo estimado: " + etaMinutes + " minutos");
        System.out.println("Observação: o endereço final do cliente não está disponível no pedido.");
    }
}
