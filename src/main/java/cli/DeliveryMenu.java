package cli;

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
            System.out.println("Status: " + (delivery.isActive() ? "Ativo" : "Inativo"));
            if (delivery.getCurrentLocation() != null) {
                System.out.println("Localização: " + delivery.getCurrentLocation().getAddress());
            }
            System.out.println("1 - Atualizar Localização");
            System.out.println("2 - Ver Notificações");
            System.out.println("3 - Alternar Disponibilidade");
            System.out.println("4 - Ver Minhas Entregas");
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
        }
    }
}
