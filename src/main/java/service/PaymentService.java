package service;

import model.PaymentMethod;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class PaymentService {

    private final HttpClient httpClient;

    public PaymentService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean processPayment(double amount, PaymentMethod method) {
        System.out.println("Iniciando processamento de pagamento de R$" + String.format("%.2f", amount) + " via " + method.getDisplayName());

        if (method == PaymentMethod.CASH) {
            System.out.println("Pagamento em dinheiro selecionado. O pagamento será coletado pelo motorista.");
            return true;
        }

        // RF13 - Simulação de QR Code e Chave PIX
        if (method == PaymentMethod.PIX) {
            String pixKey = java.util.UUID.randomUUID().toString();
            System.out.println("\n--- PAGAMENTO PIX ---");
            System.out.println("Chave PIX Aleatória: " + pixKey);
            System.out.println("\nSimulação de QR Code:");
            System.out.println("█████████████████████");
            System.out.println("█ ▄▄▄▄▄ █▀█▀█ █ ▄▄▄▄▄ █");
            System.out.println("█ █   █ █▄█▄█ █ █   █ █");
            System.out.println("█ █▄▄▄█ █▀▀▀█ █ █▄▄▄█ █");
            System.out.println("█▄▄▄▄▄▄▄█ █ █ █▄▄▄▄▄▄▄█");
            System.out.println("█████████████████████");
            System.out.println("\nEscaneie o QR Code ou use a chave PIX no seu app de pagamentos.");
            System.out.print("Após realizar o pagamento, pressione ENTER para confirmar...");
            new java.util.Scanner(System.in).nextLine(); // Pausa para o usuário
            System.out.println("Confirmando pagamento...");
        }

        // Simula uma chamada para uma API de pagamento externa
        try {
            String mockApiUrl = "https://jsonplaceholder.typicode.com/posts";
            String requestBody = String.format("{\"amount\": %.2f, \"paymentMethod\": \"%s\"}", amount, method.name());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mockApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Considera sucesso se a API retornar um status 2xx (ex: 201 Created)
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Pagamento com " + method.getDisplayName() + " processado com sucesso (simulado).");
                return true;
            } else {
                System.out.println("Falha ao processar o pagamento com " + method.getDisplayName() + " (simulado). Status: " + response.statusCode());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro de comunicação com a API de pagamento: " + e.getMessage());
            return false;
        }
    }
}
