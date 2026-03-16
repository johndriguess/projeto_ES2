package cli;

import repo.*;
import service.*;
import java.util.Scanner;

/**
 * Contexto compartilhado entre os menus.
 * Contém todos os repositórios e serviços necessários.
 */
public class MenuContext {
    private final UserRepository userRepo;
    private final VehicleRepository vehicleRepo;
    private final RideRepository rideRepo;
    private final RideHistoryRepository historyRepo;
    private final DeliveryRepository deliveryRepo;
    private final RestaurantRepository restaurantRepo;
    private final OrderRepository orderRepo;
    private final AvaliacaoRepository avaliacaoRepo;

    private final AuthService auth;
    private final RideService rideService;
    private final PricingService pricingService;
    private final RatingService ratingService;
    private final RideHistoryService historyService;
    private final DeliveryService deliveryService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final AvaliacaoService avaliacaoService;
    private final NotificationService notificationService;
    private final DeliveryAssignmentService assignmentService;

    private final Scanner scanner;

    public MenuContext(UserRepository userRepo, VehicleRepository vehicleRepo,
            RideRepository rideRepo, RideHistoryRepository historyRepo,
            DeliveryRepository deliveryRepo, RestaurantRepository restaurantRepo,
            OrderRepository orderRepo, AvaliacaoRepository avaliacaoRepo,
            AuthService auth, RideService rideService,
            PricingService pricingService, RatingService ratingService,
            RideHistoryService historyService, DeliveryService deliveryService,
            RestaurantService restaurantService, OrderService orderService,
            AvaliacaoService avaliacaoService,
            NotificationService notificationService, DeliveryAssignmentService assignmentService,
            Scanner scanner) {
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.rideRepo = rideRepo;
        this.historyRepo = historyRepo;
        this.deliveryRepo = deliveryRepo;
        this.restaurantRepo = restaurantRepo;
        this.orderRepo = orderRepo;
        this.avaliacaoRepo = avaliacaoRepo;
        this.auth = auth;
        this.rideService = rideService;
        this.pricingService = pricingService;
        this.ratingService = ratingService;
        this.historyService = historyService;
        this.deliveryService = deliveryService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.avaliacaoService = avaliacaoService;
        this.notificationService = notificationService;
        this.assignmentService = assignmentService;
        this.scanner = scanner;
    }

    public UserRepository getUserRepo() {
        return userRepo;
    }

    public VehicleRepository getVehicleRepo() {
        return vehicleRepo;
    }

    public RideRepository getRideRepo() {
        return rideRepo;
    }

    public RideHistoryRepository getHistoryRepo() {
        return historyRepo;
    }

    public DeliveryRepository getDeliveryRepo() {
        return deliveryRepo;
    }

    public RestaurantRepository getRestaurantRepo() {
        return restaurantRepo;
    }

    public OrderRepository getOrderRepo() {
        return orderRepo;
    }

    public AvaliacaoRepository getAvaliacaoRepo() {
        return avaliacaoRepo;
    }

    public AuthService getAuth() {
        return auth;
    }

    public RideService getRideService() {
        return rideService;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public RatingService getRatingService() {
        return ratingService;
    }

    public RideHistoryService getHistoryService() {
        return historyService;
    }

    public DeliveryService getDeliveryService() {
        return deliveryService;
    }

    public RestaurantService getRestaurantService() {
        return restaurantService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public AvaliacaoService getAvaliacaoService() {
        return avaliacaoService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public DeliveryAssignmentService getAssignmentService() {
        return assignmentService;
    }

    public Scanner getScanner() {
        return scanner;
    }
}
