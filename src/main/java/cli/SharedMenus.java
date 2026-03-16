package cli;

import model.*;
import service.RestaurantDetails;
import util.ValidationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Funcionalidades compartilhadas entre os menus
 */
public class SharedMenus {
    public static void rateRide(MenuContext context, User user) throws ValidationException, IOException {
        System.out.println("=== Avaliar Corrida ===");
        List<Ride> ridesToRate = MenuSelectionHelper.getRidesForUser(context, user).stream()
                .filter(ride -> ride.getStatus() == Ride.RideStatus.FINALIZADA)
                .filter(ride -> {
                    if (user instanceof Passenger) {
                        return !ride.hasPassengerRated();
                    }
                    if (user instanceof Driver) {
                        return !ride.hasDriverRated();
                    }
                    return false;
                })
                .collect(Collectors.toList());

        Ride ride = MenuSelectionHelper.selectRideFromList(context, ridesToRate, "Selecione a corrida para avaliar");
        if (ride == null) {
            return;
        }

        int rating = 0;
        while (rating < 1 || rating > 5) {
            System.out.print("Qual sua nota (de 1 a 5)? ");
            try {
                rating = Integer.parseInt(context.getScanner().nextLine().trim());
                if (rating < 1 || rating > 5) {
                    System.out.println("Nota inválida. Digite um valor entre 1 e 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }

        if (user instanceof Passenger && user.getId().equals(ride.getPassengerId())) {
            context.getRatingService().rateDriver(ride, rating);
            System.out.println("Motorista avaliado com sucesso!");
        } else if (user instanceof Driver && user.getId().equals(ride.getDriverId())) {
            context.getRatingService().ratePassenger(ride, rating);
            System.out.println("Passageiro avaliado com sucesso!");
        } else {
            System.out.println("Erro: Você não é o passageiro ou o motorista desta corrida.");
        }
    }

    public static void generateOrViewReceipt(MenuContext context, User user) throws ValidationException {
        System.out.println("=== Gerar / Enviar / Visualizar Recibo ===");
        Ride ride = MenuSelectionHelper.selectRideFromList(context, MenuSelectionHelper.getRidesForUser(context, user),
                "Selecione a corrida para gerar/visualizar recibo");
        if (ride == null) {
            return;
        }
        String rideId = ride.getId();
        String paymentMethod = ride.getPaymentMethod() != null
                ? ride.getPaymentMethod().getDisplayName()
                : "Não informado";

        try {
            context.getRideService().emitReceiptForRide(rideId, paymentMethod);

            String receiptsFolder = "receipts";
            File folder = new File(receiptsFolder);
            if (!folder.exists()) {
                System.out.println("Nenhum recibo salvo encontrado.");
                return;
            }

            File[] files = folder.listFiles((dir, name) -> name.contains(rideId));
            if (files == null || files.length == 0) {
                System.out.println("Nenhum arquivo de recibo encontrado para a corrida " + rideId);
                return;
            }

            System.out.println("Recibos encontrados:");
            for (int i = 0; i < files.length; i++) {
                System.out.printf("%d) %s\n", i + 1, files[i].getName());
            }
            System.out.print("Escolha número para visualizar (ou ENTER para voltar): ");
            String choice = context.getScanner().nextLine().trim();
            if (choice.isEmpty())
                return;
            int idx = Integer.parseInt(choice) - 1;
            if (idx < 0 || idx >= files.length) {
                System.out.println("Opção inválida.");
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(files[idx].getPath())));
            System.out.println("\n--- Conteúdo do Recibo ---\n");
            System.out.println(content);
            System.out.println("\n--- Fim do Recibo ---\n");
        } catch (IOException ioe) {
            System.out.println("Erro de I/O ao gerar/visualizar recibo: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public static void showRideHistory(MenuContext context, User user) {
        System.out.println("=== Histórico de Corridas ===");
        System.out.println("1 - Ver meu histórico");
        System.out.println("2 - Ver histórico por categoria");
        System.out.println("3 - Ver histórico por período");
        System.out.println("4 - Ver estatísticas por categoria");
        System.out.println("0 - Voltar");
        System.out.print("> ");

        String choice = context.getScanner().nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    showMyHistory(context, user);
                    break;
                case "2":
                    showHistoryByCategory(context);
                    break;
                case "3":
                    showHistoryByDateRange(context);
                    break;
                case "4":
                    showCategoryStatistics(context);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        } catch (ValidationException ve) {
            System.out.println("Erro: " + ve.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    private static void showMyHistory(MenuContext context, User user) throws ValidationException {
        List<RideHistory> history;
        if (user instanceof Passenger) {
            history = context.getHistoryService().getHistoryByPassenger(user.getEmail());
        } else if (user instanceof Driver) {
            history = context.getHistoryService().getHistoryByDriver(user.getEmail());
        } else {
            System.out.println("Tipo de usuário não suportado para histórico.");
            return;
        }
        System.out.println(context.getHistoryService().formatHistoryList(history));
    }

    private static void showHistoryByCategory(MenuContext context) throws ValidationException {
        System.out.println("Categorias disponíveis:");
        List<String> categories = context.getHistoryService().getAvailableCategories();
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + " - " + categories.get(i));
        }

        System.out.print("Escolha uma categoria (número): ");
        try {
            int choice = Integer.parseInt(context.getScanner().nextLine().trim()) - 1;
            if (choice >= 0 && choice < categories.size()) {
                String category = categories.get(choice);
                List<RideHistory> history = context.getHistoryService().getHistoryByCategory(category);
                System.out.println(context.getHistoryService().formatHistoryList(history));
            } else {
                System.out.println("Opção inválida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        }
    }

    private static void showHistoryByDateRange(MenuContext context) throws ValidationException {
        System.out.print("Digite a data de início (dd/MM/yyyy): ");
        String startDateStr = context.getScanner().nextLine().trim();

        System.out.print("Digite a data de fim (dd/MM/yyyy): ");
        String endDateStr = context.getScanner().nextLine().trim();

        try {
            java.time.LocalDate startDate = java.time.LocalDate.parse(startDateStr,
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            java.time.LocalDate endDate = java.time.LocalDate.parse(endDateStr,
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            java.time.LocalDateTime startDateTime = startDate.atStartOfDay();
            java.time.LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            List<RideHistory> history = context.getHistoryService().getHistoryByDateRange(startDateTime, endDateTime);
            System.out.println(context.getHistoryService().formatHistoryList(history));
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Formato de data inválido. Use dd/MM/yyyy.");
        }
    }

    private static void showCategoryStatistics(MenuContext context) {
        System.out.println(context.getHistoryService().formatCategoryStatistics());
    }

    public static void viewNotifications(MenuContext context, String recipientId) {
        System.out.println("=== Ver Notificações ===");

        List<Notification> notifications = context.getNotificationService().getNotificationsByRecipient(recipientId);

        if (notifications.isEmpty()) {
            System.out.println("\nVocê não tem notificações.");
            return;
        }

        System.out.println("\n--- Suas Notificações ---");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            String status = n.isRead() ? "[LIDA]" : "[NÃO LIDA]";
            System.out.printf("\n%d) %s %s\n", i + 1, status, n.getTimestamp().format(formatter));
            System.out.println("   " + n.getMessage());
        }
        System.out.println("\n------------------------");

        System.out.print("\nMarcar todas como lidas? (s/n): ");
        String markRead = context.getScanner().nextLine().trim().toLowerCase();
        if (markRead.equals("s")) {
            context.getNotificationService().markAllAsRead(recipientId);
            System.out.println("Todas as notificações marcadas como lidas.");
        }
    }

    public static void listAvailableRestaurants(MenuContext context) {
        System.out.println("=== Listar Restaurantes Disponíveis ===");

        System.out.print("Endereço da sua localização: ");
        String address = context.getScanner().nextLine().trim();

        System.out.print("Latitude (opcional, 0 se desconhecido): ");
        double lat = 0;
        try {
            lat = Double.parseDouble(context.getScanner().nextLine().trim());
        } catch (NumberFormatException e) {
            lat = 0;
        }

        System.out.print("Longitude (opcional, 0 se desconhecido): ");
        double lon = 0;
        try {
            lon = Double.parseDouble(context.getScanner().nextLine().trim());
        } catch (NumberFormatException e) {
            lon = 0;
        }

        System.out.print("Raio de busca (km, default 10): ");
        double radius = 10;
        try {
            String radiusStr = context.getScanner().nextLine().trim();
            if (!radiusStr.isEmpty()) {
                radius = Double.parseDouble(radiusStr);
            }
        } catch (NumberFormatException e) {
            radius = 10;
        }

        try {
            Location clientLocation = new Location(address, "", lat, lon);
            List<Restaurant> restaurants = context.getRestaurantService().findAvailableRestaurants(clientLocation,
                    radius);

            if (restaurants.isEmpty()) {
                System.out.println("\nNenhum restaurante disponível na sua região.");
                return;
            }

            System.out.println("\n--- Restaurantes Disponíveis ---");
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant r = restaurants.get(i);
                double distance = r.getLocation().distanceTo(clientLocation);
                System.out.printf("\n%d) %s\n", i + 1, r.getName());
                System.out.printf("   Email: %s\n", r.getEmail());
                System.out.printf("   Endereço: %s\n", r.getLocation().getAddress());
                System.out.printf("   Distância: %.2f km\n", distance);
                System.out.printf("   Status: %s | Aberto: %s\n",
                        r.isActive() ? "Ativo" : "Inativo",
                        r.isOpen() ? "Sim" : "Não");
                System.out.printf("   Itens no cardápio: %d\n", r.getMenu().size());
            }
            System.out.println("\n--------------------------------");

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor numérico inválido.");
        }
    }

    public static void viewRestaurantDetails(MenuContext context) {
        System.out.println("=== Ver Cardápio e Detalhes ===");
        String restaurantId = MenuSelectionHelper.selectRestaurantFromList(context);
        if (restaurantId == null)
            return;

        System.out.print("Distância até o restaurante (km): ");
        double distance = 5;
        try {
            distance = Double.parseDouble(context.getScanner().nextLine().trim());
        } catch (NumberFormatException e) {
            distance = 5;
        }

        try {
            RestaurantDetails details = context.getRestaurantService().getRestaurantDetails(restaurantId, distance);

            System.out.println("\n--- Detalhes do Restaurante ---");
            System.out.println("Nome: " + details.getRestaurant().getName());
            System.out.println("Endereço: " + details.getRestaurant().getLocation().getAddress());
            System.out.printf("Taxa de entrega: R$ %.2f\n", details.getDeliveryFee());
            System.out.printf("Tempo estimado: %d minutos\n", details.getDeliveryTime());

            System.out.println("\n--- Cardápio ---");
            List<MenuItem> menu = details.getMenu();
            if (menu.isEmpty()) {
                System.out.println("Nenhum item no cardápio.");
            } else {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.get(i);
                    System.out.printf("\n%d) %s - R$ %.2f\n", i + 1, item.getName(), item.getPrice());
                    System.out.printf("   %s\n", item.getDescription());
                }
            }
            System.out.println("\n-------------------------------");

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Distância inválida.");
        }
    }

    public static void createImmediateOrder(MenuContext context) {
        System.out.println("=== Fazer Pedido Imediato ===");

        try {
            String restaurantId = MenuSelectionHelper.selectRestaurantFromList(context);
            if (restaurantId == null)
                return;

            System.out.print("Digite seu email: ");
            String customerEmail = context.getScanner().nextLine().trim();
            if (customerEmail.isEmpty()) {
                System.out.println("Email é obrigatório. Pedido cancelado.");
                return;
            }

            Restaurant restaurant = context.getRestaurantRepo().findById(restaurantId)
                    .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

            List<MenuItem> selectedItems = MenuSelectionHelper.selectMenuItems(context, restaurant);
            if (selectedItems.isEmpty()) {
                System.out.println("Nenhum item selecionado. Pedido cancelado.");
                return;
            }

            System.out.print("\nDistância até o restaurante (km): ");
            double distance = 5;
            try {
                distance = Double.parseDouble(context.getScanner().nextLine().trim());
            } catch (NumberFormatException e) {
                distance = 5;
            }

            System.out.print("Desconto (R$, default 0): ");
            double discount = 0;
            try {
                String discountStr = context.getScanner().nextLine().trim();
                if (!discountStr.isEmpty()) {
                    discount = Double.parseDouble(discountStr);
                }
            } catch (NumberFormatException e) {
                discount = 0;
            }

            Order order = context.getOrderService().createOrder(restaurantId, customerEmail, selectedItems, distance,
                    discount);

            OrderFlowHelper.displayOrderSummary(order, restaurant);

            System.out.print("\nConfirmar pedido? (s/n): ");
            String confirm = context.getScanner().nextLine().trim().toLowerCase();
            if (confirm.equals("s")) {
                OrderFlowHelper.confirmAndAssignOrder(context, order, restaurant, distance);
            } else {
                System.out.println("Pedido não confirmado.");
            }

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor numérico inválido.");
        }
    }

    public static void createScheduledOrder(MenuContext context) {
        System.out.println("=== Fazer Pedido Agendado ===");

        try {
            String restaurantId = MenuSelectionHelper.selectRestaurantFromList(context);
            if (restaurantId == null)
                return;

            System.out.print("Digite seu email: ");
            String customerEmail = context.getScanner().nextLine().trim();
            if (customerEmail.isEmpty()) {
                System.out.println("Email é obrigatório. Pedido cancelado.");
                return;
            }

            Restaurant restaurant = context.getRestaurantRepo().findById(restaurantId)
                    .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

            List<MenuItem> selectedItems = MenuSelectionHelper.selectMenuItems(context, restaurant);
            if (selectedItems.isEmpty()) {
                System.out.println("Nenhum item selecionado. Pedido cancelado.");
                return;
            }

            System.out.print("\nDistância até o restaurante (km): ");
            double distance = 5;
            try {
                distance = Double.parseDouble(context.getScanner().nextLine().trim());
            } catch (NumberFormatException e) {
                distance = 5;
            }

            System.out.print("Desconto (R$, default 0): ");
            double discount = 0;
            try {
                String discountStr = context.getScanner().nextLine().trim();
                if (!discountStr.isEmpty()) {
                    discount = Double.parseDouble(discountStr);
                }
            } catch (NumberFormatException e) {
                discount = 0;
            }

            System.out.print("\nData e hora do agendamento (formato: dd/MM/yyyy HH:mm): ");
            String dateTimeStr = context.getScanner().nextLine().trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime scheduledTime;
            try {
                scheduledTime = LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de data inválido. Use: dd/MM/yyyy HH:mm");
                return;
            }

            Order order = context.getOrderService().createScheduledOrder(restaurantId, customerEmail,
                    selectedItems, distance, discount, scheduledTime);

            System.out.println("\n--- Resumo do Pedido Agendado ---");
            System.out.println("Tipo: " + order.getOrderType().getDisplayName());
            System.out.println("Agendado para: " + order.getScheduledTime().format(formatter));
            OrderFlowHelper.displayOrderSummary(order, restaurant);

            System.out.print("\nConfirmar pedido agendado? (s/n): ");
            String confirm = context.getScanner().nextLine().trim().toLowerCase();
            if (confirm.equals("s")) {
                OrderFlowHelper.confirmAndAssignOrder(context, order, restaurant, distance);
            } else {
                System.out.println("Pedido não confirmado.");
            }

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor numérico inválido.");
        }
    }

    public static void viewOrderStatus(MenuContext context, String customerEmail) {
        System.out.println("=== Consultar Status do Pedido ===");
        List<Order> customerOrders = context.getOrderRepo().findAll().stream()
                .filter(order -> order.getCustomerEmail() != null
                        && customerEmail != null
                        && order.getCustomerEmail().equalsIgnoreCase(customerEmail))
                .collect(Collectors.toList());
        Order selectedOrder = MenuSelectionHelper.selectOrderFromList(context, customerOrders, "Selecione um pedido");
        if (selectedOrder == null) {
            return;
        }

        try {
            Order order = context.getOrderService().findById(selectedOrder.getId());
            System.out.println("Pedido " + order.getId() + " status: " + order.getStatus());
        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
