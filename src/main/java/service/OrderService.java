package service;

import model.MenuItem;
import model.Order;
import model.OrderStatus;
import model.OrderType;
import model.Restaurant;
import repo.OrderRepository;
import repo.RestaurantRepository;
import util.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // agora exige e-mail do cliente que está fazendo o pedido
    public Order createOrder(String restaurantId,
            String customerEmail,
            List<MenuItem> items,
            double distance,
            double discount) {

        if (customerEmail == null || customerEmail.isBlank()) {
            throw new ValidationException("Email do cliente é obrigatório.");
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Pedido deve conter pelo menos um item.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

        double subtotal = calculateSubtotal(items);
        double deliveryFee = restaurantService.calculateDeliveryFee(distance);
        double total = calculateTotal(subtotal, deliveryFee, discount);

        Order order = new Order(restaurant.getId(), customerEmail, items);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotal(total);

        orderRepository.save(order);

        return order;
    }

    public void confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));

        if (!order.isAwaitingConfirmation()) {
            throw new ValidationException("Pedido não está aguardando confirmação.");
        }

        order.confirm(); // sets CONFIRMADO

        if (notificationService != null) {
            notificationService.removeNotificationsByRecipientAndOrder(order.getRestaurantId(), order.getId());
        }

        // RF22 - Notificar restaurante quando pedido for confirmado
        if (notificationService != null) {
            Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                    .orElse(null);

            if (restaurant != null) {
                String orderDetails = String.format("%d itens - Total: R$ %.2f",
                        order.getItems().size(), order.getTotal());
                notificationService.notifyRestaurant(
                        restaurant.getId(),
                        order.getId(),
                        orderDetails);
            }
            // também notificar cliente
            notificationService.notifyCustomer(
                    order.getCustomerEmail(),
                    order.getId(),
                    "Seu pedido foi CONFIRMADO pelo restaurante.");
        }

        // avançar automaticamente para preparação
        order.setStatus(OrderStatus.PREPARACAO);
        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(), "Pedido em preparação.");
        }

        orderRepository.update(order);
    }

    // RF23 - Criar pedido agendado (com email do cliente)
    public Order createScheduledOrder(String restaurantId,
            String customerEmail,
            List<MenuItem> items,
            double distance,
            double discount,
            LocalDateTime scheduledTime) {

        if (scheduledTime == null) {
            throw new ValidationException("Horário agendado é obrigatório para pedidos agendados.");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Horário agendado não pode ser no passado.");
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Pedido deve conter pelo menos um item.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

        double subtotal = calculateSubtotal(items);
        double deliveryFee = restaurantService.calculateDeliveryFee(distance);
        double total = calculateTotal(subtotal, deliveryFee, discount);

        Order order = new Order(restaurant.getId(), customerEmail, items, OrderType.AGENDADO, scheduledTime);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotal(total);

        orderRepository.save(order);

        return order;
    }

    // RF23 - Criar pedido imediato (método original mantido para compatibilidade)
    public Order createImmediateOrder(String restaurantId,
            String customerEmail,
            List<MenuItem> items,
            double distance,
            double discount) {

        Order order = createOrder(restaurantId, customerEmail, items, distance, discount);
        order.setOrderType(OrderType.IMEDIATO);
        return order;
    }

    // Buscar pedidos agendados
    public List<Order> getScheduledOrders() {
        return orderRepository.findAll().stream()
                .filter(Order::isScheduled)
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar pedidos imediatos
    public List<Order> getImmediateOrders() {
        return orderRepository.findAll().stream()
                .filter(Order::isImmediate)
                .collect(java.util.stream.Collectors.toList());
    }

    // RF24? - Listar pedidos pendentes (AGUARDANDO_CONFIRMACAO) de um restaurante
    public List<Order> getPendingOrdersForRestaurant(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new ValidationException("ID do restaurante é obrigatório.");
        }

        // valida existência do restaurante
        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new ValidationException("Restaurante não encontrado.");
        }

        return orderRepository.findAll().stream()
                .filter(o -> restaurantId.equals(o.getRestaurantId()))
                .filter(Order::isAwaitingConfirmation)
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar pedido por ID
    public void rejectOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));

        if (!order.isAwaitingConfirmation()) {
            throw new ValidationException("Pedido não está aguardando confirmação.");
        }

        order.reject();

        if (notificationService != null) {
            notificationService.removeNotificationsByRecipientAndOrder(order.getRestaurantId(), order.getId());
            // notificar restaurante também? poderia ser redundante
            notificationService.notifyCustomer(
                    order.getCustomerEmail(),
                    order.getId(),
                    "Seu pedido foi REJEITADO pelo restaurante.");
        }

        orderRepository.update(order);
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));
    }

    // fornece status atual do pedido
    public OrderStatus getOrderStatus(String orderId) {
        return findById(orderId).getStatus();
    }

    // transições de status
    public void startPreparation(String orderId) {
        Order order = findById(orderId);
        if (!order.isConfirmed()) {
            throw new ValidationException("Pedido precisa ser confirmado antes de iniciar preparação.");
        }
        order.setStatus(OrderStatus.PREPARACAO);
        orderRepository.update(order);
        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(), "Pedido em preparação.");
        }
    }

    public void markReady(String orderId) {
        Order order = findById(orderId);
        if (!order.isPreparing()) {
            throw new ValidationException("Pedido deve estar em preparação para ser marcado como pronto.");
        }
        order.setStatus(OrderStatus.PRONTO);
        orderRepository.update(order);
        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(), "Pedido pronto para retirada.");
        }
    }

    public void dispatchOrder(String orderId) {
        Order order = findById(orderId);
        if (order.isAwaitingDeliveryAcceptance()) {
            throw new ValidationException("Pedido aguarda aceite do entregador.");
        }
        if (!order.isReady() && !order.isConfirmed()) {
            throw new ValidationException("Pedido deve estar pronto para ser despachado.");
        }
        order.setStatus(OrderStatus.EM_ENTREGA);
        orderRepository.update(order);
        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(), "Pedido em entrega.");
        }
    }

    public void acceptOrderByDelivery(String orderId, String deliveryId) {
        if (deliveryId == null || deliveryId.isBlank()) {
            throw new ValidationException("ID do entregador é obrigatório.");
        }

        Order order = findById(orderId);

        if (!order.isAwaitingDeliveryAcceptance()) {
            throw new ValidationException("Pedido não está aguardando aceite do entregador.");
        }

        if (order.getAssignedDeliveryId() == null || !deliveryId.equals(order.getAssignedDeliveryId())) {
            throw new ValidationException("Este pedido não está atribuído ao entregador informado.");
        }

        order.setStatus(OrderStatus.EM_ENTREGA);
        orderRepository.update(order);

        if (notificationService != null) {
            notificationService.removeNotificationsByRecipientAndOrder(deliveryId, order.getId());
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(),
                    "Seu pedido foi ACEITO pelo entregador e está em entrega.");
        }
    }

    public void rejectOrderByDelivery(String orderId, String deliveryId) {
        if (deliveryId == null || deliveryId.isBlank()) {
            throw new ValidationException("ID do entregador é obrigatório.");
        }

        Order order = findById(orderId);

        if (!order.isAwaitingDeliveryAcceptance()) {
            throw new ValidationException("Pedido não está aguardando aceite do entregador.");
        }

        if (order.getAssignedDeliveryId() == null || !deliveryId.equals(order.getAssignedDeliveryId())) {
            throw new ValidationException("Este pedido não está atribuído ao entregador informado.");
        }

        order.setAssignedDeliveryId(null);
        order.setStatus(OrderStatus.REJEITADO);
        orderRepository.update(order);

        if (notificationService != null) {
            notificationService.removeNotificationsByRecipientAndOrder(deliveryId, order.getId());
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(),
                    "Seu pedido foi REJEITADO pelo entregador.");
        }
    }

    public void deliverOrder(String orderId) {
        Order order = findById(orderId);
        if (!order.isOutForDelivery()) {
            throw new ValidationException("Pedido não está em entrega.");
        }
        order.setStatus(OrderStatus.AGUARDANDO_CONFIRMACAO_CLIENTE);
        orderRepository.update(order);
        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(),
                    "Pedido entregue ao destino. Confirme o recebimento para finalizar.");
        }
    }

    public void confirmDeliveryByCustomer(String orderId, String customerEmail) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new ValidationException("Email do cliente é obrigatório.");
        }

        Order order = findById(orderId);

        if (!customerEmail.equalsIgnoreCase(order.getCustomerEmail())) {
            throw new ValidationException("Este pedido não pertence ao cliente informado.");
        }

        if (!order.isAwaitingCustomerConfirmation() && !order.isOutForDelivery()) {
            throw new ValidationException("Pedido não está apto para confirmação de entrega.");
        }

        order.setStatus(OrderStatus.ENTREGUE);
        orderRepository.update(order);

        if (notificationService != null) {
            notificationService.notifyCustomer(order.getCustomerEmail(), order.getId(),
                    "Pedido confirmado e finalizado. Obrigado!");
        }
    }

    public double calculateSubtotal(List<MenuItem> items) {
        return items.stream()
                .mapToDouble(MenuItem::getPrice)
                .sum();
    }

    public double calculateTotal(double subtotal, double deliveryFee, double discount) {
        return subtotal + deliveryFee - discount;
    }
}