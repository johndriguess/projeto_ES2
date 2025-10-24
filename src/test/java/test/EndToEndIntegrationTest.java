package test;

import model.*;
import repo.RideHistoryRepository;
import repo.RideRepository;
import repo.UserRepository;
import repo.VehicleRepository;
import service.AuthService;
import service.PricingService;
import service.RatingService;
import service.RideService;
import util.ValidationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração end-to-end que percorre o fluxo principal:
 * cadastrar passageiro/motorista, adicionar veículo, solicitar corrida,
 * atribuir motorista, processar pagamento, emitir recibo e avaliar.
 */
public class EndToEndIntegrationTest {

    private UserRepository userRepo;
    private VehicleRepository vehicleRepo;
    private RideRepository rideRepo;
    private RideHistoryRepository historyRepo;
    private PricingService pricingService;
    private AuthService authService;
    private RideService rideService;
    private RatingService ratingService;

    private File userDb = new File("e2e_users.date");
    private File vehicleDb = new File("e2e_vehicles.db");
    private File rideDb = new File("e2e_rides.db");
    private File historyDb = new File("e2e_history.db");

    @BeforeEach
    public void setUp() throws IOException, ValidationException {
        // limpa arquivos de teste
        userDb.delete();
        vehicleDb.delete();
        rideDb.delete();
        historyDb.delete();

        userRepo = new UserRepository(userDb);
        vehicleRepo = new VehicleRepository(vehicleDb.getPath());
        rideRepo = new RideRepository(rideDb.getPath());
        historyRepo = new RideHistoryRepository(historyDb.getPath());
        pricingService = new PricingService();
        authService = new AuthService(userRepo, vehicleRepo);
        rideService = new RideService(rideRepo, userRepo, pricingService);
        rideService.setHistoryRepository(historyRepo);
        ratingService = new RatingService(userRepo, rideRepo);
    }

    @AfterEach
    public void tearDown() {
        userDb.delete();
        vehicleDb.delete();
        rideDb.delete();
        historyDb.delete();
    }

    @Test
    public void testFullRideFlow() throws ValidationException, IOException {
        // 1) cadastrar passageiro
        Passenger p = authService.registerPassenger("E2E Passenger", "e2e_pass@example.com", "+5511999000000", "pass123");

        // 2) cadastrar motorista com veículo e categoria UBER_X
        Driver d = authService.registerDriver("E2E Driver", "e2e_driver@example.com", "+5511999000001", "pass123", "CNH-1", "E2E-PLT", "ModelX", 2020, "Branco");
        // garante que o veículo exista e tenha categoria compatível
        d.getVehicle().setCategory(VehicleCategory.UBER_X.name());
        // coloca o motorista próximo do local de origem
        d.setCurrentLocation(new Location("Rua Origem"));
        userRepo.update(d);

        // 3) solicitar corrida; como existe motorista compatível, espera-se atribuição imediata
    Ride ride = rideService.createRideRequest(p.getEmail(), "Rua Origem", "Rua Destino", VehicleCategory.UBER_X.name(), PaymentMethod.CASH);

        assertNotNull(ride);
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus(), "Esperava-se que a corrida fosse aceita pois há motorista disponível");
        assertNotNull(ride.getDriverId(), "Motorista deveria ter sido atribuído");

        // Confirma que o motorista foi marcado como indisponível
        Driver assigned = (Driver) userRepo.findById(ride.getDriverId());
        assertFalse(assigned.isAvailable(), "Motorista deveria estar indisponível após atribuição");

        // 4) processar pagamento (simula captura)
        boolean paid = rideService.processRidePayment(ride.getId());
        assertTrue(paid, "Pagamento deveria ser processado com sucesso");

        // 5) emitir recibo (deve finalizar a corrida e liberar o motorista)
    rideService.emitReceiptForRide(ride.getId(), "Dinheiro");

        // Verifica status finalizado
        Ride after = rideRepo.findById(ride.getId());
        assertEquals(Ride.RideStatus.FINALIZADA, after.getStatus());

        // Motorista deve estar disponível novamente
        Driver driverAfter = (Driver) userRepo.findById(assigned.getId());
        assertTrue(driverAfter.isAvailable(), "Motorista deveria ser liberado após emissão do recibo");

        // 6) histórico deve conter um registro
        List<model.RideHistory> histories = historyRepo.findByPassengerEmail(p.getEmail());
        assertFalse(histories.isEmpty(), "Histórico do passageiro não deve estar vazio após finalização");

        // 7) avaliar motorista (agora que a corrida está finalizada)
        ratingService.rateDriver(after, 5);
        Driver driverRated = (Driver) userRepo.findById(assigned.getId());
        assertTrue(driverRated.getAverageRating() >= 5.0, "Motorista deveria ter recebido a avaliação de 5 estrelas");
    }
}
