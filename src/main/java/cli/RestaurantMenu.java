package cli;

import model.*;
import util.ValidationException;
import java.util.List;

/**
 * Menu específico para restaurantes
 */
public class RestaurantMenu {
    private final MenuContext context;
    private final Restaurant restaurant;

    public RestaurantMenu(MenuContext context, Restaurant restaurant) {
        this.context = context;
        this.restaurant = restaurant;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Menu Restaurante - " + restaurant.getName() + " ===");
            double avg = context.getAvaliacaoService().getAverageRatingForRestaurant(restaurant.getId());
            int count = context.getAvaliacaoService().getTotalRatingsForRestaurant(restaurant.getId());
            System.out.printf("Avaliação média: %.2f (%d avaliações)\n", avg, count);
            System.out.println("Status: " + (restaurant.isActive() ? "Ativo" : "Inativo") +
                    " | Aberto: " + (restaurant.isOpen() ? "Sim" : "Não"));
            System.out.println("1 - Ver Pedidos Pendentes");
            System.out.println("2 - Atualizar Status do Pedido");
            System.out.println("3 - Ver Notificações");
            System.out.println("4 - Gerenciar Cardápio");
            System.out.println("5 - Alternar Status (Ativo/Inativo)");
            System.out.println("6 - Alternar Funcionamento (Aberto/Fechado)");
            System.out.println("7 - Avaliar Cliente/Entregador");
            System.out.println("0 - Sair");
            System.out.print("> ");

            String opt = context.getScanner().nextLine().trim();

            try {
                switch (opt) {
                    case "1":
                        listPendingOrders();
                        break;
                    case "2":
                        updateOrderStatus();
                        break;
                    case "3":
                        viewNotifications();
                        break;
                    case "4":
                        manageMenu();
                        break;
                    case "5":
                        toggleActive();
                        break;
                    case "6":
                        toggleOpen();
                        break;
                    case "7":
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

    private void listPendingOrders() {
        System.out.println("=== Pedidos Pendentes ===");

        try {
            List<Order> pendings = context.getOrderService().getPendingOrdersForRestaurant(restaurant.getId());
            if (pendings.isEmpty()) {
                System.out.println("Nenhum pedido pendente.");
                return;
            }

            System.out.println();
            for (Order o : pendings) {
                System.out.printf("ID: %s - Total: R$ %.2f - Itens: %d - Tipo: %s\n",
                        o.getId(), o.getTotal(), o.getItems().size(),
                        o.isImmediate() ? "Imediato" : "Agendado");
            }

            System.out.print("\nDeseja confirmar ou rejeitar algum pedido? (c/r/n): ");
            String action = context.getScanner().nextLine().trim().toLowerCase();

            if (action.equals("c") || action.equals("r")) {
                System.out.print("Escolha o número do pedido: ");
                int choice = Integer.parseInt(context.getScanner().nextLine().trim());
                if (choice < 1 || choice > pendings.size()) {
                    System.out.println("Opção inválida.");
                    return;
                }
                String orderId = pendings.get(choice - 1).getId();

                if (action.equals("c")) {
                    context.getOrderService().confirmOrder(orderId);
                    System.out.println("Pedido confirmado.");
                } else {
                    context.getOrderService().rejectOrder(orderId);
                    System.out.println("Pedido rejeitado.");
                }
            }
        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void updateOrderStatus() {
        System.out.println("=== Atualizar Status do Pedido ===");
        try {
            List<Order> restaurantOrders = context.getOrderRepo().findAll().stream()
                    .filter(order -> restaurant.getId().equals(order.getRestaurantId()))
                    .collect(java.util.stream.Collectors.toList());

            if (restaurantOrders.isEmpty()) {
                System.out.println("Nenhum pedido encontrado para este restaurante.");
                return;
            }

            System.out.println("--- Selecione o pedido ---");
            for (int i = 0; i < restaurantOrders.size(); i++) {
                Order order = restaurantOrders.get(i);
                System.out.printf("%d) Status: %s | Total: R$ %.2f\n",
                        i + 1,
                        order.getStatus(),
                        order.getTotal());
            }
            System.out.print("Escolha (número) ou 0 para cancelar: ");
            int option = Integer.parseInt(context.getScanner().nextLine().trim());
            if (option == 0) {
                System.out.println("Operação cancelada.");
                return;
            }
            if (option < 1 || option > restaurantOrders.size()) {
                System.out.println("Opção inválida.");
                return;
            }

            String orderId = restaurantOrders.get(option - 1).getId();
            Order order = context.getOrderService().findById(orderId);
            System.out.println("Status atual: " + order.getStatus());
            System.out.println("Opções de atualização:");
            System.out.println("1 - Preparação");
            System.out.println("2 - Pronto");
            System.out.println("3 - Em entrega");
            System.out.println("4 - Entregue");
            System.out.print("Escolha: ");

            String choice = context.getScanner().nextLine().trim();

            switch (choice) {
                case "1":
                    context.getOrderService().startPreparation(orderId);
                    System.out.println("Status definido para PREPARACAO");
                    break;
                case "2":
                    context.getOrderService().markReady(orderId);
                    System.out.println("Status definido para PRONTO");
                    break;
                case "3":
                    context.getOrderService().dispatchOrder(orderId);
                    System.out.println("Status definido para EM_ENTREGA");
                    break;
                case "4":
                    context.getOrderService().deliverOrder(orderId);
                    System.out.println("Status definido para ENTREGUE");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void viewNotifications() {
        SharedMenus.viewNotifications(context, restaurant.getId());
    }

    private void manageMenu() {
        System.out.println("\n=== Gerenciar Cardápio ===");
        System.out.println("1 - Ver cardápio atual");
        System.out.println("2 - Adicionar item");
        System.out.println("3 - Remover item");
        System.out.print("Escolha: ");

        String opt = context.getScanner().nextLine().trim();

        switch (opt) {
            case "1":
                showMenu();
                break;
            case "2":
                addMenuItem();
                break;
            case "3":
                removeMenuItem();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void showMenu() {
        List<MenuItem> menu = restaurant.getMenu();
        if (menu.isEmpty()) {
            System.out.println("Cardápio vazio.");
            return;
        }

        System.out.println("\n--- Cardápio ---");
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            System.out.printf("%d) %s - R$ %.2f\n", i + 1, item.getName(), item.getPrice());
            System.out.printf("   %s\n", item.getDescription());
        }
    }

    private void addMenuItem() {
        System.out.print("Nome do item: ");
        String name = context.getScanner().nextLine().trim();

        System.out.print("Descrição: ");
        String desc = context.getScanner().nextLine().trim();

        System.out.print("Preço (R$): ");
        double price = Double.parseDouble(context.getScanner().nextLine().trim());

        MenuItem item = new MenuItem(name, desc, price);
        restaurant.addMenuItem(item);
        context.getRestaurantRepo().update(restaurant);

        System.out.println("Item adicionado com sucesso!");
    }

    private void removeMenuItem() {
        showMenu();
        System.out.print("\nNúmero do item a remover (0 para cancelar): ");
        int choice = Integer.parseInt(context.getScanner().nextLine().trim());

        if (choice == 0)
            return;

        List<MenuItem> menu = restaurant.getMenu();
        if (choice < 1 || choice > menu.size()) {
            System.out.println("Opção inválida.");
            return;
        }

        MenuItem removed = menu.remove(choice - 1);
        context.getRestaurantRepo().update(restaurant);
        System.out.println("Item '" + removed.getName() + "' removido com sucesso!");
    }

    private void toggleActive() {
        if (restaurant.isActive()) {
            restaurant.deactivate();
            System.out.println("Restaurante desativado.");
        } else {
            restaurant.activate();
            System.out.println("Restaurante reativado.");
        }
        context.getRestaurantRepo().update(restaurant);
    }

    private void toggleOpen() {
        if (restaurant.isOpen()) {
            restaurant.close();
            System.out.println("Restaurante fechado.");
        } else {
            restaurant.open();
            System.out.println("Restaurante aberto.");
        }
        context.getRestaurantRepo().update(restaurant);
    }

    private void rateOrderParticipants() {
        SharedMenus.restaurantRateOrder(context, restaurant);
    }
}
