# 🚖 UberPB – Sistema de Mobilidade Simplificado

## 📌 Sobre o Projeto
O **UberPB** e **Uber Eats** é um sistema acadêmico desenvolvido para a disciplina **Engenharia de Software 2 (2025.2)** da **UEPB**.  
Ele é inspirado no funcionamento do Uber, mas com foco em **projeto, arquitetura e qualidade de código**, não em interfaces gráficas.  

O sistema conta com três tipos de usuários:
- **Passageiro/Cliente** → solicita corridas.  
- **Motorista** → aceita corridas e transporta passageiros.  
- **Administrador** → gerencia usuários, pagamentos e suporte.  
- **Entregador** → faz entrega de deliveries no Uber Eats.
- **Restaurante** → oferta comidas e bebidas no Uber Eats.
---

## ✅ Requisitos Funcionais
### Cadastro e Autenticação
- RF01: Cadastro de passageiros, motoristas, restaurantes e  entregadores. 
- RF02: Validação de documentos e veículo para categorias (UberX, Comfort, Black, etc.) e entregadores.
- RF03: Login com e-mail.

### Solicitação de Corrida
- RF04: Passageiro informa origem e destino.
- RF05: Cálculo de tempo e preço estimados.
- RF06: Opções de categorias (X, Comfort, Black, Bag, XL).  
- RF07: Notificação a motoristas da categoria.
  
### Solicitação de Pedido (Uber Eats) 
- RF19: O passageiro (cliente) deve poder acessar restaurantes disponíveis próximos.
- RF20: O cliente deve poder visualizar cardápios, preços, taxas de entrega e tempo  estimado. 
- RF21: O cliente deve poder montar o pedido e confirmar a compra. 
- RF22: O sistema deve notificar o restaurante e o entregador mais próximo sobre o pedido.
- RF23: O sistema deve permitir pedidos agendados ou imediatos.  

### Aceite da Corrida (ou Pedido)
- RF08: Motorista pode aceitar ou recusar.
- RF24: O entregador deve poder aceitar ou recusar pedidos de entrega.
- RF25: O restaurante deve poder confirmar ou rejeitar pedidos recebidos.
- RF09: Corrida atribuída ao motorista mais próximo.  

### Acompanhamento da corrida
- RF10: Passageiro acompanha localização do motorista.  
- RF11: Estimativa de chegada atualizada.
- RF26: O cliente deve acompanhar o status do pedido (preparação, retirada, entrega, …).
- RF27: O entregador deve visualizar a rota até o restaurante e até o cliente.   
- RF12: Motorista visualiza rota otimizada.  

### Pagamentos
- RF13: Pagamentos via Cartão, PIX, ou dinheiro  
- RF14: Cálculo do valor (distância, tempo, categoria, tarifa dinâmica)  
- RF15: Emissão de recibo eletrônico.
        Pedido Uber Eats: valor do pedido, taxa de entrega e tarifa dinâmica. 

### Avaliações
- RF16: Passageiros e motoristas podem se avaliar mutuamente.  
- RF17: Média das avaliações influencia prioridade.
- RF28: O sistema deve permitir que clientes, entregadores e restaurantes se avaliem  mutuamente.  

### Histórico
- RF18: Histórico de corridas filtrável por categoria   

---

## 🛠️ Tecnologias Utilizadas
- **Java** (linguagem principal)  
- **JUnit** (testes unitários – mínimo de 80% de cobertura)  
- **Emma Plugin** (relatórios de cobertura de testes no Eclipse)  
- **Padrões de Projeto (Design Patterns)**  
- **Persistência em disco (arquivos locais)**  
- **Interface de comandos (console)**  
