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
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PROCESSAMENTO DE PAGAMENTO");
        System.out.println("=".repeat(50));
        System.out.println("Valor: R$ " + String.format("%.2f", amount));
        System.out.println("Método: " + method.getDisplayName());
        System.out.println("=".repeat(50));

        if (method == PaymentMethod.CASH) {
            System.out.println("\nPAGAMENTO EM DINHEIRO");
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│  O pagamento será coletado pelo     │");
            System.out.println("│  motorista ao final da corrida.    │");
            System.out.println("│                                     │");
            System.out.println("│  Dica: Tenha o valor exato          │");
            System.out.println("│      para facilitar o troco!        │");
            System.out.println("└─────────────────────────────────────┘");
            return true;
        }

        // RF13 - Simulação de QR Code e Chave PIX
        if (method == PaymentMethod.PIX) {
            String pixKey = java.util.UUID.randomUUID().toString();
            System.out.println("\nPAGAMENTO PIX");
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│  Chave PIX: " + pixKey.substring(0, 8) + "... │");
            System.out.println("└─────────────────────────────────────┘");

            System.out.println("\nQR CODE PARA PAGAMENTO:");
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│ ████████████████████████████████ │");
            System.out.println("│ █ ▄▄▄▄▄ █▀█▀█ █ ▄▄▄▄▄ █ ▄▄▄▄▄ █ │");
            System.out.println("│ █ █   █ █▄█▄█ █ █   █ █ █   █ █ │");
            System.out.println("│ █ █▄▄▄█ █▀▀▀█ █ █▄▄▄█ █ █▄▄▄█ █ │");
            System.out.println("│ █▄▄▄▄▄▄█ █ █ █▄▄▄▄▄▄▄█ █ █ █ │");
            System.out.println("│ ████████████████████████████████ │");
            System.out.println("└─────────────────────────────────────┘");

            System.out.println("\nINSTRUÇÕES:");
            System.out.println("1. Abra seu app de pagamentos");
            System.out.println("2. Escaneie o QR Code acima");
            System.out.println("3. Ou digite a chave PIX: " + pixKey);
            System.out.println("4. Confirme o pagamento");

            System.out.print("\nApós realizar o pagamento, pressione ENTER para confirmar...");
            new java.util.Scanner(System.in).nextLine();
            System.out.println("Pagamento confirmado!");
        }

        // Simulação para Cartão de Crédito
        if (method == PaymentMethod.CREDIT_CARD) {
            System.out.println("\nPAGAMENTO COM CARTÃO DE CRÉDITO");
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│  Processando pagamento...          │");
            System.out.println("│                                     │");
            System.out.println("│  Dados seguros protegidos           │");
            System.out.println("│  Aguarde a confirmação...           │");
            System.out.println("└─────────────────────────────────────┘");

            // Simular delay de processamento
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Simulação para PayPal
        if (method == PaymentMethod.PAYPAL) {
            System.out.println("\nPAGAMENTO VIA PAYPAL");
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│  Redirecionando para PayPal...     │");
            System.out.println("│                                     │");
            System.out.println("│  Abra o link no navegador           │");
            System.out.println("│  Faça login na sua conta            │");
            System.out.println("│  Confirme o pagamento               │");
            System.out.println("└─────────────────────────────────────┘");

            System.out.print("\nApós confirmar no PayPal, pressione ENTER...");
            new java.util.Scanner(System.in).nextLine();
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
                System.out.println("Falha ao processar o pagamento com " + method.getDisplayName()
                        + " (simulado). Status: " + response.statusCode());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro de comunicação com a API de pagamento: " + e.getMessage());
            return false;
        }
    }
}
