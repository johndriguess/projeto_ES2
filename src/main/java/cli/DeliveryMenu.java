package cli;

import java.util.List;

import model.*;
import util.ValidationException;

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
            System.out.println("4 - Ver Minhas Entregas");
            System.out.println("5 - Ver Pedidos Disponíveis (Aceitar/Recusar)");
            System.out.println("6 - Atualizar Status da Entrega (Sair p/ Entrega / Entregue)");
            System.out.println("7 - Avaliar Cliente/Restaurante");
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

    private void manageAvailableOrders() {
        if (!delivery.isActive()) {
            System.out.println("Você está inativo. Alterne sua disponibilidade primeiro.");
            return;
        }
        if (delivery.getCurrentLocation() == null) {
            System.out.println("Por favor, atualize sua localização primeiro.");
            return;
        }

        System.out.println("=== Pedidos Disponíveis na Região ===");
        // Define o raio como 10km da localização do entregador
        List<Order> available = context.getOrderService().getAvailableOrdersForDelivery(
            delivery.getId(), delivery.getCurrentLocation().getAddress(), 10.0); 

        if (available.isEmpty()) {
            System.out.println("Nenhum pedido disponível no momento.");
            return;
        }

        for (int i = 0; i < available.size(); i++) {
            Order o = available.get(i);
            System.out.printf("%d) Pedido ID: %s | Ganho Est.: R$ %.2f\n", i + 1, o.getId(), o.getDeliveryFee());
            System.out.printf("   Origem: %s\n", o.getOrigin());
            System.out.printf("   Destino: %s\n", o.getDestination());
        }

        System.out.print("\nEscolha um pedido (número) ou 0 para voltar: ");
        int choice = Integer.parseInt(context.getScanner().nextLine().trim());
        if (choice < 1 || choice > available.size()) return;

        Order selected = available.get(choice - 1);
        System.out.print("Deseja (A)ceitar ou (R)ecusar o pedido? ");
        String action = context.getScanner().nextLine().trim().toUpperCase();

        if (action.equals("A")) {
            context.getOrderService().acceptOrderByDelivery(selected.getId(), delivery.getId());
            System.out.println("Pedido ACEITO com sucesso! Dirija-se ao restaurante.");
        } else if (action.equals("R")) {
            context.getOrderService().refuseOrderByDelivery(selected.getId(), delivery.getId());
            System.out.println("Pedido RECUSADO.");
        }
    }

    private void updateDeliveryStatus() {
        System.out.println("=== Atualizar Status da Entrega ===");
        List<Order> myOrders = context.getOrderRepo().findAll().stream()
            .filter(o -> delivery.getId().equals(o.getAssignedDeliveryId()))
            .filter(o -> o.getStatus() == OrderStatus.ACEITO || o.getStatus() == OrderStatus.EM_ENTREGA)
            .collect(java.util.stream.Collectors.toList());

        if (myOrders.isEmpty()) {
            System.out.println("Nenhuma entrega em andamento (com status ACEITO ou EM_ENTREGA).");
            return;
        }

        for (int i = 0; i < myOrders.size(); i++) {
            Order o = myOrders.get(i);
            System.out.printf("%d) Pedido ID: %s | Status Atual: %s\n", i + 1, o.getId(), o.getStatus());
        }
        
        System.out.print("\nEscolha um pedido (número) ou 0 para voltar: ");
        int choice = Integer.parseInt(context.getScanner().nextLine().trim());
        if (choice < 1 || choice > myOrders.size()) return;

        Order selected = myOrders.get(choice - 1);

        System.out.println("1 - Marcar como Saiu para Entrega (EM_ENTREGA)");
        System.out.println("2 - Marcar como Concluído (ENTREGUE)");
        System.out.print("> ");
        String action = context.getScanner().nextLine().trim();

        if (action.equals("1")) {
            context.getOrderService().dispatchOrder(selected.getId());
            System.out.println("Status atualizado para EM_ENTREGA.");
        } else if (action.equals("2")) {
            context.getOrderService().deliverOrder(selected.getId());
            System.out.println("Entrega concluída! Bom trabalho.");
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
        }
    }

    private void rateOrderParticipants() {
        SharedMenus.deliveryRateOrder(context, delivery);
    }
}
