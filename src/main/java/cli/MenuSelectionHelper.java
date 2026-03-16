package cli;

import model.MenuItem;
import model.Order;
import model.Restaurant;
import model.Ride;
import model.User;
import util.ValidationException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class MenuSelectionHelper {
    private MenuSelectionHelper() {
    }

    static List<Ride> getRidesForUser(MenuContext context, User user) {
        return context.getRideRepo().findAll().stream()
                .filter(ride -> {
                    if (user instanceof model.Passenger) {
                        return user.getId().equals(ride.getPassengerId());
                    }
                    if (user instanceof model.Driver) {
                        return user.getId().equals(ride.getDriverId());
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Ride::getRequestTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());
    }

    static Ride selectRideFromList(MenuContext context, List<Ride> rides, String title) {
        if (rides == null || rides.isEmpty()) {
            System.out.println("Nenhuma corrida disponível para seleção.");
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < rides.size(); i++) {
            Ride ride = rides.get(i);
            String when = ride.getRequestTime() != null ? ride.getRequestTime().format(formatter) : "Sem horário";
            System.out.printf("%d) %s -> %s | Status: %s | Data: %s\n",
                    i + 1,
                    ride.getOrigin().getAddress(),
                    ride.getDestination().getAddress(),
                    ride.getStatus().getDisplayName(),
                    when);
        }

        System.out.print("Escolha uma corrida (número) ou 0 para cancelar: ");
        try {
            int choice = Integer.parseInt(context.getScanner().nextLine().trim());
            if (choice == 0) {
                System.out.println("Operação cancelada.");
                return null;
            }
            if (choice < 1 || choice > rides.size()) {
                System.out.println("Opção inválida.");
                return null;
            }
            return rides.get(choice - 1);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
            return null;
        }
    }

    static Order selectOrderFromList(MenuContext context, List<Order> orders, String title) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("Nenhum pedido disponível para seleção.");
            return null;
        }

        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            String type = order.isImmediate() ? "Imediato" : "Agendado";
            System.out.printf("%d) Status: %s | Tipo: %s | Total: R$ %.2f\n",
                    i + 1,
                    order.getStatus(),
                    type,
                    order.getTotal());
        }

        System.out.print("Escolha um pedido (número) ou 0 para cancelar: ");
        try {
            int choice = Integer.parseInt(context.getScanner().nextLine().trim());
            if (choice == 0) {
                System.out.println("Operação cancelada.");
                return null;
            }
            if (choice < 1 || choice > orders.size()) {
                System.out.println("Opção inválida.");
                return null;
            }
            return orders.get(choice - 1);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
            return null;
        }
    }

    static String selectRestaurantFromList(MenuContext context) {
        System.out.println("\n--- Selecionar Restaurante ---");
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
            model.Location userLocation = new model.Location(address, "", lat, lon);
            List<Restaurant> availableRestaurants = context.getRestaurantService()
                    .findAvailableRestaurants(userLocation, radius);

            if (availableRestaurants.isEmpty()) {
                System.out.println("Nenhum restaurante disponível na sua região.");
                return null;
            }

            System.out.println("\n--- Restaurantes Disponíveis ---");
            for (int i = 0; i < availableRestaurants.size(); i++) {
                Restaurant r = availableRestaurants.get(i);
                double distance = userLocation.distanceTo(r.getLocation());
                System.out.printf("%d) %s\n", i + 1, r.getName());
                System.out.printf("   Endereço: %s\n", r.getLocation().getAddress());
                System.out.printf("   Distância: %.2f km\n", distance);
                System.out.printf("   Itens no cardápio: %d\n", r.getMenu().size());
                System.out.println();
            }

            System.out.print("Escolha um restaurante (número) ou 0 para cancelar: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(context.getScanner().nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Número inválido.");
                return null;
            }

            if (choice == 0) {
                System.out.println("Seleção cancelada.");
                return null;
            }

            if (choice < 1 || choice > availableRestaurants.size()) {
                System.out.println("Opção inválida.");
                return null;
            }

            Restaurant selected = availableRestaurants.get(choice - 1);
            System.out.println("\nRestaurante selecionado: " + selected.getName());
            return selected.getId();

        } catch (ValidationException e) {
            System.out.println("Erro: " + e.getMessage());
            return null;
        }
    }

    static List<MenuItem> selectMenuItems(MenuContext context, Restaurant restaurant) {
        System.out.println("\n--- Cardápio de " + restaurant.getName() + " ---");
        List<MenuItem> menu = restaurant.getMenu();
        if (menu.isEmpty()) {
            System.out.println("Restaurante sem itens no cardápio.");
            return new java.util.ArrayList<>();
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            System.out.printf("%d) %s - R$ %.2f\n", i + 1, item.getName(), item.getPrice());
        }

        List<MenuItem> selectedItems = new java.util.ArrayList<>();
        System.out.println("\nSelecione os itens (digite o número, ou 0 para finalizar):");
        while (true) {
            System.out.print("Item: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(context.getScanner().nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Número inválido.");
                continue;
            }

            if (choice == 0) {
                break;
            }
            if (choice < 1 || choice > menu.size()) {
                System.out.println("Opção inválida.");
                continue;
            }

            selectedItems.add(menu.get(choice - 1));
            System.out.println("Item adicionado: " + menu.get(choice - 1).getName());
        }

        return selectedItems;
    }
}
