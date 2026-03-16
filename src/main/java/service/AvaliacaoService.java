package service;

import model.Avaliacao;
import model.Order;
import model.OrderStatus;
import repo.AvaliacaoRepository;
import repo.OrderRepository;
import util.ValidationException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;
    private final OrderRepository orderRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, OrderRepository orderRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.orderRepository = orderRepository;
    }

    public List<Order> getDeliveredOrdersForCustomer(String customerEmail) {
        String normalizedEmail = normalizeEmail(customerEmail);
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.ENTREGUE)
                .filter(order -> order.getCustomerEmail() != null
                        && order.getCustomerEmail().trim().toLowerCase(Locale.ROOT).equals(normalizedEmail))
                .collect(Collectors.toList());
    }

    public List<Order> getDeliveredOrdersForDelivery(String deliveryId) {
        String normalizedDeliveryId = normalizeId(deliveryId, "ID do entregador é obrigatório.");
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.ENTREGUE)
                .filter(order -> normalizedDeliveryId.equals(order.getAssignedDeliveryId()))
                .collect(Collectors.toList());
    }

    public List<Order> getDeliveredOrdersForRestaurant(String restaurantId) {
        String normalizedRestaurantId = normalizeId(restaurantId, "ID do restaurante é obrigatório.");
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.ENTREGUE)
                .filter(order -> normalizedRestaurantId.equals(order.getRestaurantId()))
                .collect(Collectors.toList());
    }

    public void customerRatesDelivery(String orderId, String customerEmail, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedCustomerEmail = normalizeEmail(customerEmail);

        if (order.getCustomerEmail() == null
                || !order.getCustomerEmail().trim().toLowerCase(Locale.ROOT).equals(normalizedCustomerEmail)) {
            throw new ValidationException("Este pedido não pertence ao cliente informado.");
        }
        if (order.getAssignedDeliveryId() == null || order.getAssignedDeliveryId().isBlank()) {
            throw new ValidationException("Este pedido não possui entregador atribuído.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.CLIENTE,
                normalizedCustomerEmail,
                Avaliacao.TipoParte.ENTREGADOR,
                order.getAssignedDeliveryId(),
                nota,
                comentario);
    }

    public void customerRatesRestaurant(String orderId, String customerEmail, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedCustomerEmail = normalizeEmail(customerEmail);

        if (order.getCustomerEmail() == null
                || !order.getCustomerEmail().trim().toLowerCase(Locale.ROOT).equals(normalizedCustomerEmail)) {
            throw new ValidationException("Este pedido não pertence ao cliente informado.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.CLIENTE,
                normalizedCustomerEmail,
                Avaliacao.TipoParte.RESTAURANTE,
                order.getRestaurantId(),
                nota,
                comentario);
    }

    public void deliveryRatesCustomer(String orderId, String deliveryId, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedDeliveryId = normalizeId(deliveryId, "ID do entregador é obrigatório.");

        if (!normalizedDeliveryId.equals(order.getAssignedDeliveryId())) {
            throw new ValidationException("Este pedido não está atribuído ao entregador informado.");
        }
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new ValidationException("Pedido sem cliente válido para avaliação.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.ENTREGADOR,
                normalizedDeliveryId,
                Avaliacao.TipoParte.CLIENTE,
                order.getCustomerEmail().trim().toLowerCase(Locale.ROOT),
                nota,
                comentario);
    }

    public void deliveryRatesRestaurant(String orderId, String deliveryId, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedDeliveryId = normalizeId(deliveryId, "ID do entregador é obrigatório.");

        if (!normalizedDeliveryId.equals(order.getAssignedDeliveryId())) {
            throw new ValidationException("Este pedido não está atribuído ao entregador informado.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.ENTREGADOR,
                normalizedDeliveryId,
                Avaliacao.TipoParte.RESTAURANTE,
                order.getRestaurantId(),
                nota,
                comentario);
    }

    public void restaurantRatesCustomer(String orderId, String restaurantId, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedRestaurantId = normalizeId(restaurantId, "ID do restaurante é obrigatório.");

        if (!normalizedRestaurantId.equals(order.getRestaurantId())) {
            throw new ValidationException("Este pedido não pertence ao restaurante informado.");
        }
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new ValidationException("Pedido sem cliente válido para avaliação.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.RESTAURANTE,
                normalizedRestaurantId,
                Avaliacao.TipoParte.CLIENTE,
                order.getCustomerEmail().trim().toLowerCase(Locale.ROOT),
                nota,
                comentario);
    }

    public void restaurantRatesDelivery(String orderId, String restaurantId, int nota, String comentario) {
        Order order = validateDeliveredOrder(orderId);
        String normalizedRestaurantId = normalizeId(restaurantId, "ID do restaurante é obrigatório.");

        if (!normalizedRestaurantId.equals(order.getRestaurantId())) {
            throw new ValidationException("Este pedido não pertence ao restaurante informado.");
        }
        if (order.getAssignedDeliveryId() == null || order.getAssignedDeliveryId().isBlank()) {
            throw new ValidationException("Este pedido não possui entregador atribuído.");
        }

        saveUniqueRating(order,
                Avaliacao.TipoParte.RESTAURANTE,
                normalizedRestaurantId,
                Avaliacao.TipoParte.ENTREGADOR,
                order.getAssignedDeliveryId(),
                nota,
                comentario);
    }

    public double getAverageRatingForCustomer(String customerEmail) {
        String normalizedEmail = normalizeEmail(customerEmail);
        return calculateAverage(Avaliacao.TipoParte.CLIENTE, normalizedEmail);
    }

    public double getAverageRatingForDelivery(String deliveryId) {
        String normalizedDeliveryId = normalizeId(deliveryId, "ID do entregador é obrigatório.");
        return calculateAverage(Avaliacao.TipoParte.ENTREGADOR, normalizedDeliveryId);
    }

    public double getAverageRatingForRestaurant(String restaurantId) {
        String normalizedRestaurantId = normalizeId(restaurantId, "ID do restaurante é obrigatório.");
        return calculateAverage(Avaliacao.TipoParte.RESTAURANTE, normalizedRestaurantId);
    }

    public int getTotalRatingsForCustomer(String customerEmail) {
        String normalizedEmail = normalizeEmail(customerEmail);
        return countRatings(Avaliacao.TipoParte.CLIENTE, normalizedEmail);
    }

    public int getTotalRatingsForDelivery(String deliveryId) {
        String normalizedDeliveryId = normalizeId(deliveryId, "ID do entregador é obrigatório.");
        return countRatings(Avaliacao.TipoParte.ENTREGADOR, normalizedDeliveryId);
    }

    public int getTotalRatingsForRestaurant(String restaurantId) {
        String normalizedRestaurantId = normalizeId(restaurantId, "ID do restaurante é obrigatório.");
        return countRatings(Avaliacao.TipoParte.RESTAURANTE, normalizedRestaurantId);
    }

    private Order validateDeliveredOrder(String orderId) {
        String normalizedOrderId = normalizeId(orderId, "ID do pedido é obrigatório.");
        Order order = orderRepository.findById(normalizedOrderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));

        if (order.getStatus() != OrderStatus.ENTREGUE) {
            throw new ValidationException("A avaliação só é permitida para pedidos ENTREGUES.");
        }
        return order;
    }

    private void saveUniqueRating(Order order,
            Avaliacao.TipoParte avaliadorTipo,
            String avaliadorId,
            Avaliacao.TipoParte alvoTipo,
            String alvoId,
            int nota,
            String comentario) {
        validateScore(nota);

        boolean exists = avaliacaoRepository.findAll().stream()
                .anyMatch(av -> av.getOrderId().equals(order.getId())
                        && av.getAvaliadorTipo() == avaliadorTipo
                        && av.getAvaliadorId().equals(avaliadorId)
                        && av.getAlvoTipo() == alvoTipo
                        && av.getAlvoId().equals(alvoId));

        if (exists) {
            throw new ValidationException("Você já avaliou este alvo neste pedido.");
        }

        Avaliacao avaliacao = new Avaliacao(
                order.getId(),
                avaliadorTipo,
                avaliadorId,
                alvoTipo,
                alvoId,
                nota,
                comentario);
        avaliacaoRepository.save(avaliacao);
    }

    private void validateScore(int nota) {
        if (nota < 1 || nota > 5) {
            throw new ValidationException("A nota deve estar entre 1 e 5.");
        }
    }

    private double calculateAverage(Avaliacao.TipoParte alvoTipo, String alvoId) {
        List<Avaliacao> ratings = avaliacaoRepository.findAll().stream()
                .filter(av -> av.getAlvoTipo() == alvoTipo)
                .filter(av -> av.getAlvoId().equals(alvoId))
                .collect(Collectors.toList());

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);
    }

    private int countRatings(Avaliacao.TipoParte alvoTipo, String alvoId) {
        return (int) avaliacaoRepository.findAll().stream()
                .filter(av -> av.getAlvoTipo() == alvoTipo)
                .filter(av -> av.getAlvoId().equals(alvoId))
                .count();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeId(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
