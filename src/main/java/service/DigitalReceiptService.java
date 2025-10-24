package service;

import model.Receipt;
import model.Ride;
import model.User;
import model.Driver;
import model.Passenger;
import model.PricingInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class DigitalReceiptService {

    private final String receiptsFolder = "data" + File.separator + "receipts";

    public DigitalReceiptService() {
        File f = new File(receiptsFolder);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * Gera um objeto Receipt contendo o texto do recibo (plain-text),
     * com as informações mínimas obrigatórias.
     */
    public Receipt generateReceipt(Ride ride, Passenger passenger, Driver driver, PricingInfo pricing, String paymentMethod) {
        String rideId = ride.getId();
        String id = "receipt-" + rideId + "-" + UUID.randomUUID().toString().substring(0, 8);
        String passengerName = passenger != null ? passenger.getName() : "(desconhecido)";
        String passengerEmail = passenger != null ? passenger.getEmail() : "(desconhecido)";
        String driverName = driver != null ? driver.getName() : "(não atribuído)";
        String driverEmail = driver != null ? driver.getEmail() : "(não atribuído)";
        double total = pricing != null ? pricing.getTotalPrice() : 0.0;
        String details = buildDetails(ride, pricing);

        StringBuilder sb = new StringBuilder();
        sb.append("=== RECIBO ELETRÔNICO ===\n");
        sb.append("Recibo ID: ").append(id).append("\n");
        sb.append("Corrida ID: ").append(rideId).append("\n");
        sb.append("Gerado em: ").append(Instant.now().toString()).append("\n\n");

        sb.append("--- Passageiro ---\n");
        sb.append("Nome: ").append(passengerName).append("\n");
        sb.append("Email: ").append(passengerEmail).append("\n\n");

        sb.append("--- Motorista ---\n");
        sb.append("Nome: ").append(driverName).append("\n");
        sb.append("Email: ").append(driverEmail).append("\n\n");

        sb.append("--- Corrida ---\n");
        sb.append(details).append("\n");

        sb.append("--- Pagamento ---\n");
        sb.append("Forma: ").append(paymentMethod == null ? "Não informada" : paymentMethod).append("\n");
        sb.append(String.format("Valor total: R$ %.2f\n", total));
        sb.append("\n");
        sb.append("Obrigado por utilizar o serviço.\n");

        String content = sb.toString();
        return new Receipt(id, rideId, passengerName, passengerEmail, driverName, driverEmail,
                paymentMethod == null ? "N/A" : paymentMethod, total, details, content, Instant.now());
    }

    private String buildDetails(Ride ride, PricingInfo pricing) {
        StringBuilder d = new StringBuilder();
        d.append("Origem: ").append(ride.getOrigin() != null ? ride.getOrigin().getAddress() : "(não definido)").append("\n");
        d.append("Destino: ").append(ride.getDestination() != null ? ride.getDestination().getAddress() : "(não definido)").append("\n");
        if (pricing != null) {
            d.append(String.format("Distância estimada: %s\n", pricing.getFormattedDistance()));
            d.append(String.format("Tempo estimado: %s\n", pricing.getFormattedTime()));
            d.append(String.format("Categoria: %s\n", pricing.getCategory()));
        }
        if (ride.getStatus() != null) {
            d.append("Status da corrida: ").append(ride.getStatus().getDisplayName()).append("\n");
        }
        if (ride.getOptimizedRoute() != null && !ride.getOptimizedRoute().isEmpty()) {
            d.append("Rota (resumo): ").append(ride.getOptimizedRoute().get(0)).append(" ...\n");
        }
        return d.toString();
    }

    /**
     * "Envia" o recibo ao passageiro:
     * - Se o passageiro tem email, grava um arquivo em receipts/{rideId}.txt e imprime log simulando envio.
     * - Caso contrário, grava o arquivo e retorna caminho.
     */
    public void sendReceiptToPassenger(Receipt receipt) throws IOException {
        String filename = receiptsFolder + File.separator + receipt.getRideId() + "-" + receipt.getId() + ".txt";
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(receipt.getContent());
        }
        // Simula envio por e-mail / in-app (apenas log)
        System.out.println("Recibo gerado e salvo em: " + filename);
        System.out.println("Recibo enviado para: " + receipt.getPassengerEmail() + " (simulação).");
    }
}
