package cli;

import model.MenuItem;
import model.Order;
import model.Restaurant;

final class OrderFlowHelper {
    private OrderFlowHelper() {
    }

    static void displayOrderSummary(Order order, Restaurant restaurant) {
        System.out.println("\n--- Resumo do Pedido ---");
        System.out.println("ID do pedido: " + order.getId());
        System.out.println("Restaurante: " + restaurant.getName());
        System.out.println("\nItens:");
        for (MenuItem item : order.getItems()) {
            System.out.printf("  - %s: R$ %.2f\n", item.getName(), item.getPrice());
        }
        System.out.printf("\nSubtotal: R$ %.2f\n", order.getSubtotal());
        System.out.printf("Taxa de entrega: R$ %.2f\n", order.getDeliveryFee());
        System.out.printf("Desconto: R$ %.2f\n", order.getDiscount());
        System.out.printf("TOTAL: R$ %.2f\n", order.getTotal());
    }

    static void confirmAndAssignOrder(MenuContext context, Order order, Restaurant restaurant, double distance) {
        System.out.println("\nPedido enviado ao restaurante com sucesso!");
        System.out.println("Status atual: " + order.getStatus());
        System.out.println("Tempo estimado (após aceite): " +
                context.getRestaurantService().calculateEstimatedTime(distance) + " minutos");
        System.out.println("Aguarde o restaurante aceitar o pedido.");
    }
}
