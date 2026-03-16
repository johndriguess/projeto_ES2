package cli;

import model.Delivery;
import model.MenuItem;
import model.Order;
import model.Restaurant;
import util.ValidationException;

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
        try {
            context.getOrderService().confirmOrder(order.getId());
            System.out.println("\nPedido confirmado com sucesso!");
            System.out.println("Tempo estimado de entrega: " +
                    context.getRestaurantService().calculateEstimatedTime(distance) + " minutos");

            System.out.println("\nBuscando entregador disponível...");
            context.getAssignmentService().assignDeliveryToOrder(
                    order,
                    restaurant.getLocation(),
                    restaurant.getName(),
                    "Endereço do cliente");

            context.getOrderRepo().update(order);

            Delivery delivery = context.getDeliveryRepo().findById(order.getAssignedDeliveryId())
                    .orElse(null);
            if (delivery != null) {
                System.out.println("\nEntregador atribuído:");
                System.out.println("   Nome: " + delivery.getName());
                System.out.println("   Telefone: " + delivery.getPhone());
            }
        } catch (ValidationException e) {
            System.out.println("Aviso: " + e.getMessage());
            System.out.println("Pedido confirmado, mas será necessário atribuir entregador manualmente.");
        }
    }
}
