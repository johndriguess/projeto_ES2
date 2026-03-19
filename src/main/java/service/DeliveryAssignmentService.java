package service;

import model.Delivery;
import model.DeliveryStatus;
import model.Location;
import model.Order;
import model.OrderStatus;
import repo.DeliveryRepository;
import util.ValidationException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveryAssignmentService {

    private final DeliveryRepository deliveryRepository;
    private final NotificationService notificationService;

    public DeliveryAssignmentService(DeliveryRepository deliveryRepository,
            NotificationService notificationService) {
        this.deliveryRepository = deliveryRepository;
        this.notificationService = notificationService;
    }

    // RF22 - Selecionar o entregador mais próximo disponível
    public Delivery findNearestAvailableDelivery(Location restaurantLocation) {

        if (restaurantLocation == null) {
            throw new ValidationException("Localização do restaurante é obrigatória.");
        }

        List<Delivery> availableDeliveries = deliveryRepository.findAll().stream()
                .filter(d -> d.isActive())
                .filter(d -> d.getValidationStatus() == DeliveryStatus.APROVADO)
                .filter(d -> d.getCurrentLocation() != null)
                .collect(Collectors.toList());

        if (availableDeliveries.isEmpty()) {
            throw new ValidationException("Nenhum entregador disponível no momento.");
        }

        // Ordenar por distância e retornar o mais próximo
        Optional<Delivery> nearest = availableDeliveries.stream()
                .min(Comparator.comparingDouble(d -> d.getCurrentLocation().distanceTo(restaurantLocation)));

        return nearest.orElseThrow(() -> new ValidationException("Não foi possível encontrar entregador disponível."));
    }

    // RF22 - Atribuir entregador ao pedido e notificar
    public void assignDeliveryToOrder(Order order,
            Location restaurantLocation,
            String restaurantName,
            String deliveryAddress) {

        if (order == null) {
            throw new ValidationException("Pedido é obrigatório.");
        }

        if (!order.isConfirmed()) {
            throw new ValidationException("Pedido precisa estar confirmado para atribuir entregador.");
        }

        // Encontrar entregador mais próximo
        Delivery nearestDelivery = findNearestAvailableDelivery(restaurantLocation);

        // Atribuir entregador ao pedido
        order.setAssignedDeliveryId(nearestDelivery.getId());
        order.setStatus(OrderStatus.AGUARDANDO_ACEITE_ENTREGADOR);

        // Notificar o entregador
        notificationService.notifyDelivery(
                nearestDelivery.getId(),
                order.getId(),
                restaurantName,
                deliveryAddress);
    }

    // Buscar entregadores disponíveis em um raio específico
    public List<Delivery> findAvailableDeliveriesInRadius(Location location, double radiusKm) {

        if (location == null) {
            throw new ValidationException("Localização é obrigatória.");
        }

        return deliveryRepository.findAll().stream()
                .filter(d -> d.isActive())
                .filter(d -> d.getValidationStatus() == DeliveryStatus.APROVADO)
                .filter(d -> d.getCurrentLocation() != null)
                .filter(d -> d.getCurrentLocation().distanceTo(location) <= radiusKm)
                .collect(Collectors.toList());
    }

    // Calcular distância entre entregador e localização
    public double calculateDistance(String deliveryId, Location targetLocation) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ValidationException("Entregador não encontrado."));

        if (delivery.getCurrentLocation() == null) {
            throw new ValidationException("Localização do entregador não disponível.");
        }

        return delivery.getCurrentLocation().distanceTo(targetLocation);
    }
}
