package test;

import model.MenuItem;
import model.Order;
import model.OrderStatus;
import repo.AvaliacaoRepository;
import repo.OrderRepository;
import service.AvaliacaoService;
import util.ValidationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AvaliacaoServiceTest {

    private File orderDb;
    private File avaliacaoDb;

    private OrderRepository orderRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private AvaliacaoService avaliacaoService;

    private Order deliveredOrder;

    @BeforeEach
    public void setup() {
        orderDb = new File("test_orders_avaliacao.db");
        avaliacaoDb = new File("test_avaliacoes.db");
        orderDb.delete();
        avaliacaoDb.delete();

        orderRepository = new OrderRepository(orderDb.getPath());
        avaliacaoRepository = new AvaliacaoRepository(avaliacaoDb.getPath());
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, orderRepository);

        deliveredOrder = new Order("rest-1", "cliente@teste.com", List.of(new MenuItem("X", "Y", 20.0)));
        deliveredOrder.setAssignedDeliveryId("del-1");
        deliveredOrder.setStatus(OrderStatus.ENTREGUE);
        orderRepository.save(deliveredOrder);
    }

    @AfterEach
    public void tearDown() {
        orderDb.delete();
        avaliacaoDb.delete();
    }

    @Test
    public void customerCanRateDeliveryAndRestaurant() {
        avaliacaoService.customerRatesDelivery(deliveredOrder.getId(), "cliente@teste.com", 5, "Ótimo");
        avaliacaoService.customerRatesRestaurant(deliveredOrder.getId(), "cliente@teste.com", 4, "Bom");

        assertEquals(5.0, avaliacaoService.getAverageRatingForDelivery("del-1"), 0.001);
        assertEquals(4.0, avaliacaoService.getAverageRatingForRestaurant("rest-1"), 0.001);
    }

    @Test
    public void deliveryCanRateCustomerAndRestaurant() {
        avaliacaoService.deliveryRatesCustomer(deliveredOrder.getId(), "del-1", 3, "Ok");
        avaliacaoService.deliveryRatesRestaurant(deliveredOrder.getId(), "del-1", 5, "Excelente");

        assertEquals(3.0, avaliacaoService.getAverageRatingForCustomer("cliente@teste.com"), 0.001);
        assertEquals(5.0, avaliacaoService.getAverageRatingForRestaurant("rest-1"), 0.001);
    }

    @Test
    public void restaurantCanRateCustomerAndDelivery() {
        avaliacaoService.restaurantRatesCustomer(deliveredOrder.getId(), "rest-1", 4, "Tudo certo");
        avaliacaoService.restaurantRatesDelivery(deliveredOrder.getId(), "rest-1", 2, "Atrasou");

        assertEquals(4.0, avaliacaoService.getAverageRatingForCustomer("cliente@teste.com"), 0.001);
        assertEquals(2.0, avaliacaoService.getAverageRatingForDelivery("del-1"), 0.001);
    }

    @Test
    public void shouldNotAllowDuplicateRatingForSameTargetInSameOrder() {
        avaliacaoService.customerRatesDelivery(deliveredOrder.getId(), "cliente@teste.com", 5, "Primeira");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> avaliacaoService.customerRatesDelivery(deliveredOrder.getId(), "cliente@teste.com", 4,
                        "Segunda"));

        assertEquals("Você já avaliou este alvo neste pedido.", ex.getMessage());
    }

    @Test
    public void shouldRequireDeliveredOrder() {
        Order pending = new Order("rest-1", "cliente@teste.com", List.of(new MenuItem("X", "Y", 20.0)));
        pending.setAssignedDeliveryId("del-1");
        pending.setStatus(OrderStatus.EM_ENTREGA);
        orderRepository.save(pending);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> avaliacaoService.customerRatesDelivery(pending.getId(), "cliente@teste.com", 5, ""));

        assertEquals("A avaliação só é permitida para pedidos ENTREGUES.", ex.getMessage());
    }

    @Test
    public void shouldCalculateAverageFromMultipleRatings() {
        Order deliveredOrder2 = new Order("rest-1", "cliente2@teste.com", List.of(new MenuItem("A", "B", 10.0)));
        deliveredOrder2.setAssignedDeliveryId("del-1");
        deliveredOrder2.setStatus(OrderStatus.ENTREGUE);
        orderRepository.save(deliveredOrder2);

        avaliacaoService.customerRatesDelivery(deliveredOrder.getId(), "cliente@teste.com", 4, "");
        avaliacaoService.customerRatesDelivery(deliveredOrder2.getId(), "cliente2@teste.com", 2, "");

        assertEquals(3.0, avaliacaoService.getAverageRatingForDelivery("del-1"), 0.001);
        assertEquals(2, avaliacaoService.getTotalRatingsForDelivery("del-1"));
    }
}
