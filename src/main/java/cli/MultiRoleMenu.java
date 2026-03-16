package cli;

import model.*;

/**
 * Menu para usuários com múltiplos papéis no sistema
 */
public class MultiRoleMenu {
    private final MenuContext context;
    private final UserProfile profile;

    public MultiRoleMenu(MenuContext context, UserProfile profile) {
        this.context = context;
        this.profile = profile;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Menu - " + profile.getPrimaryName() + " ===");
            System.out.println("Você tem múltiplos papéis:");
            for (String role : profile.getRoles()) {
                System.out.println("  • " + role);
            }
            System.out.println("\nEscolha o papel que deseja usar:");

            int option = 1;

            if (profile.isPassenger()) {
                System.out.println(option + " - Acessar como Passageiro");
                option++;
            }
            if (profile.isDriver()) {
                System.out.println(option + " - Acessar como Motorista");
                option++;
            }
            if (profile.isDelivery()) {
                System.out.println(option + " - Acessar como Entregador");
                option++;
            }
            if (profile.isRestaurant()) {
                System.out.println(option + " - Acessar como Restaurante");
                option++;
            }

            System.out.println("0 - Sair");
            System.out.print("> ");

            String choice = context.getScanner().nextLine().trim();

            try {
                int choiceNum = Integer.parseInt(choice);

                if (choiceNum == 0) {
                    System.out.println("Saindo...");
                    return;
                }

                // Mapear a escolha para o papel correto
                int currentOption = 1;

                if (profile.isPassenger() && choiceNum == currentOption) {
                    PassengerMenu passengerMenu = new PassengerMenu(context, profile.getAsPassenger());
                    passengerMenu.show();
                    return;
                }
                if (profile.isPassenger())
                    currentOption++;

                if (profile.isDriver() && choiceNum == currentOption) {
                    DriverMenu driverMenu = new DriverMenu(context, profile.getAsDriver());
                    driverMenu.show();
                    return;
                }
                if (profile.isDriver())
                    currentOption++;

                if (profile.isDelivery() && choiceNum == currentOption) {
                    DeliveryMenu deliveryMenu = new DeliveryMenu(context, profile.getDelivery());
                    deliveryMenu.show();
                    return;
                }
                if (profile.isDelivery())
                    currentOption++;

                if (profile.isRestaurant() && choiceNum == currentOption) {
                    RestaurantMenu restaurantMenu = new RestaurantMenu(context, profile.getRestaurant());
                    restaurantMenu.show();
                    return;
                }

                System.out.println("Opção inválida.");

            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }
}
