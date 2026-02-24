# Teste Manual dos Requisitos RF01, RF02, RF19, RF20, RF21, RF22 e RF23

## Instruções de Execução

```bash
cd /Users/vitor/Desktop/projeto_ES2
java -cp target/classes cli.Main
```

---

## RF01 - Cadastro de Restaurantes e Entregadores

### **Teste 1: Cadastrar Restaurante**

1. Execute o programa
2. Escolha opção **21**
3. Preencha:
   - Nome: `Pizza Top`
   - Email: `pizza@email.com`
   - CNPJ: `12345678901234` (14 dígitos)
   - Endereço: `Rua das Flores, 123`
   - Latitude: `0` (pode deixar 0)
   - Longitude: `0` (pode deixar 0)
   - Adicionar itens? **s**
   - Nome do item: `Pizza Calabresa`
   - Descrição: `Tradicional com queijo`
   - Preço: `40`
   - Nome do item: `Refrigerante`
   - Descrição: `Lata 350ml`
   - Preço: `5`
   - Nome do item: _(pressione ENTER para finalizar)_

**Resultado Esperado**: Restaurante cadastrado com sucesso. O sistema deve exibir ID, nome e status.

### **Teste 2: Validação de Email Duplicado**

1. Tente cadastrar outro restaurante com o mesmo email `pizza@email.com`

**Resultado Esperado**: Mensagem de erro informando "Email já cadastrado."

### **Teste 3: Validação de CNPJ Inválido**

1. Tente cadastrar restaurante com CNPJ: `123` (menos de 14 dígitos)

**Resultado Esperado**: Mensagem de erro informando "CNPJ inválido."

---

## RF02 - Validar Documentos de Entregadores

### **Teste 4: Cadastrar Entregador com Documentos Válidos**

1. Escolha opção **20**
2. Preencha:
   - Nome: `João Silva`
   - Email: `joao@email.com`
   - CPF: `12345678901` (11 dígitos)
   - Telefone: `11999999999`
   - CNH: `12345678901` (11 dígitos)
   - Documento do veículo: `CRLV123`

**Resultado Esperado**:

- Entregador cadastrado com sucesso.
- Status de validação: **APROVADO**

### **Teste 5: Cadastrar Entregador com CPF Inválido**

1. Escolha opção **20**
2. Preencha dados mas use CPF: `123` (inválido)

**Resultado Esperado**:

- Entregador cadastrado mas Status: **REJEITADO**

### **Teste 6: Cadastrar Entregador sem CNH**

1. Escolha opção **20**
2. Deixe CNH em branco ou com menos de 11 dígitos

**Resultado Esperado**: Mensagem de erro informando "CNH inválida."

---

## RF19 - Acessar Restaurantes Disponíveis

### **Teste 7: Listar Restaurantes por Localização**

1. **Pré-requisito**: Cadastre 2-3 restaurantes primeiro (opção 21)
2. Escolha opção **22**
3. Preencha:
   - Endereço: `Rua Principal`
   - Latitude: `0`
   - Longitude: `0`
   - Raio: `10` km

**Resultado Esperado**:

- Lista de restaurantes disponíveis
- Mostra: Nome, ID, Email, Endereço, Distância, Status, Quantidade de itens

### **Teste 8: Busca sem Resultados**

1. Escolha opção **22**
2. Use raio muito pequeno: `0.01` km

**Resultado Esperado**: "Nenhum restaurante disponível na sua região."

---

## RF20 - Visualizar Cardápio, Preços, Taxas e Tempo

### **Teste 9: Ver Detalhes do Restaurante**

1. **Pré-requisito**: Tenha um restaurante cadastrado com itens no cardápio
2. Escolha opção **23**
3. Digite o **ID do restaurante** (copie da listagem anterior)
4. Digite distância: `5` km

**Resultado Esperado**:

- Nome e endereço do restaurante
- **Taxa de entrega**: R$ 5,00 (distância ≤ 5km)
- **Tempo estimado**: 30 minutos
- Lista completa do cardápio com nomes, descrições e preços

### **Teste 10: Cálculo de Taxa por Distância**

1. Repita teste 9 com diferentes distâncias:
   - 3 km → Taxa: R$ 5,00
   - 7 km → Taxa: R$ 8,00
   - 15 km → Taxa: R$ 12,00

**Resultado Esperado**: Taxa varia conforme distância.

---

## RF21 - Montar Pedido e Confirmar Compra

### **Teste 11: Fazer Pedido Completo**

1. **Pré-requisito**: Restaurante com cardápio configurado
2. Escolha opção **24**
3. Informe sua localização:
   - Endereço: `Rua Principal`
   - Latitude: `0`
   - Longitude: `0`
   - Raio: `10` km
4. Sistema lista restaurantes disponíveis
5. Escolha o número do restaurante desejado
6. Selecione itens:
   - Item: `1` (Pizza Calabresa)
   - Item: `2` (Refrigerante)
   - Item: `0` (finalizar)
7. Distância: `5` km
8. Desconto: `5`
9. Confirmar? **s**

**Resultado Esperado**:

```
--- Resumo do Pedido ---
Itens:
  - Pizza Calabresa: R$ 40,00
  - Refrigerante: R$ 5,00

Subtotal: R$ 45,00
Taxa de entrega: R$ 5,00
Desconto: R$ 5,00
TOTAL: R$ 45,00

Pedido confirmado com sucesso.
Tempo estimado de entrega: 30 minutos
```

### **Teste 12: Cálculo Correto do Total**

Verifique a fórmula: **Total = Subtotal + Taxa de Entrega - Desconto**

### **Teste 13: Pedido sem Restaurantes Disponíveis**

1. Escolha opção **24**
2. Use raio muito pequeno: `0.01` km

**Resultado Esperado**: "Nenhum restaurante disponível na sua região."

### **Teste 13B: Cancelar Seleção de Restaurante**

1. Escolha opção **24**
2. Quando listar restaurantes, digite `0` para cancelar

**Resultado Esperado**: "Seleção cancelada."

---

## RF22 - Notificar o restaurante e o entregador mais próximo

### **Teste 14: Atualizar Localização do Entregador**

1. Pré-requisito: Cadastrar entregador (opção 20)
2. Escolha opção **28**
3. Preencha:
   - Email do entregador: (use o email cadastrado)
   - Novo endereço: `Rua Central, 100`
   - Latitude: `1`
   - Longitude: `1`

**Resultado Esperado**: Localização atualizada com sucesso.

### **Teste 15: Atribuir Entregador Mais Próximo ao Pedido**

1. Pré-requisito:
   - Restaurante cadastrado com localização
   - Entregador cadastrado com localização
   - Pedido criado e confirmado
2. Escolha opção **26**
3. Preencha:
   - ID do pedido: (ID do pedido criado)
   - ID do restaurante: (ID do restaurante)
   - Endereço de entrega: `Rua das Entregas, 456`

**Resultado Esperado**:

- Entregador atribuído com sucesso
- Sistema exibe ID, nome e telefone do entregador
- Distância do entregador até o restaurante é calculada

### **Teste 16: Ver Notificações do Entregador**

1. Escolha opção **26**
2. Digite o ID do entregador

**Resultado Esperado**:

- Lista de notificações do entregador
- Cada notificação mostra status (LIDA/NÃO LIDA)
- Mensagem contém informações do pedido
- Opção de marcar todas como lidas

### **Teste 17: Ver Notificações do Restaurante**

1. Escolha opção **26**
2. Digite o ID do restaurante

**Resultado Esperado**:

- Lista de notificações do restaurante
- Notificações sobre pedidos confirmados
- Detalhes do pedido (quantidade de itens e valor total)

### **Teste 18: Sistema Seleciona Entregador Mais Próximo Automaticamente**

1. Cadastrar 2 entregadores com localizações diferentes
   - Entregador A: lat=1, lon=1 (opção 27)
   - Entregador B: lat=50, lon=50 (opção 27)
2. Restaurante em: lat=0, lon=0
3. Criar e confirmar pedido (opção 24)

**Resultado Esperado**: Sistema automaticamente seleciona Entregador A (mais próximo) ao confirmar o pedido.

---

## RF23 - Pedidos agendados ou imediatos

### **Teste 19: Criar Pedido Imediato**

1. Escolha opção **24**
2. Siga o fluxo normal de criação de pedido
3. Observe que o tipo é IMEDIATO

**Resultado Esperado**:

- Pedido criado com tipo IMEDIATO
- Sem campo de horário agendado
- Processamento imediato

### **Teste 20: Criar Pedido Agendado**

1. Escolha opção **25**
2. Informe sua localização:
   - Endereço: `Rua Principal`
   - Latitude: `0`
   - Longitude: `0`
   - Raio: `10` km
3. Escolha um restaurante da lista
4. Preencha dados do pedido normalmente
5. Quando solicitado, informe:
   - Data e hora: `25/02/2026 14:00` (data futura)

**Resultado Esperado**:

- Pedido criado com tipo AGENDADO
- Horário agendado exibido no resumo
- Sistema confirma que pedido será preparado no horário especificado

### **Teste 21: Validação de Horário no Passado**

1. Escolha opção **25**
2. Tente agendar para uma data/hora passada

**Resultado Esperado**: Mensagem de erro informando "Horário agendado não pode ser no passado."

### **Teste 22: Formato de Data Inválido**

1. Escolha opção **25**
2. Digite formato incorreto de data: `25-02-2026` ou `14:00 25/02/2026`
   - Selecionar restaurante pela lista
   - Observar que não solicita horário
3. Criar 1 pedido agendado (opção 25)
   - Selecionar restaurante pela lista
   - Observar solicitação de horário
     **Resultado Esperado**: Mensagem de erro informando formato correto (dd/MM/yyyy HH:mm).

### **Teste 23: Diferença entre Pedidos Imediatos e Agendados**

1. Criar 1 pedido imediato (opção 24)
2. Criar 1 pedido agendado (opção 25)
3. Comparar os resumos

**Resultado Esperado**:

- Pedido imediato: Tipo = IMEDIATO, sem horário agendado
- Pedido agendado: Tipo = AGENDADO, horário agendado exibido
- Ambos calculam preços corretamente

### **Teste 24: Agendar para Horário Específico**

1. Agendar pedido para amanhã às 12:30
2. Verificar que horário está correto no resumo

**Resultado Esperado**: Sistema exibe "Agendado para: 26/02/2026 12:30" (exemplo).

---

## Checklist de Validação

- [ ] RF01: Restaurante cadastrado com sucesso
- [ ] RF01: Validações de email e CNPJ funcionando
- [ ] RF02: Entregador aprovado com documentos válidos
- [ ] RF02: Entregador rejeitado com documentos inválidos
- [ ] RF02: Validação de CNH obrigatória
- [ ] RF19: Listagem de restaurantes por localização
- [ ] RF19: Filtro por raio de busca funcionando
- [ ] RF20: Cardápio exibido corretamente
- [ ] RF20: Taxa de entrega calculada por distância
- [ ] RF20: Tempo estimado calculado
- [ ] RF21: Pedido criado com múltiplos itens
- [ ] RF21: Cálculo do total correto (subtotal + taxa - desconto)
- [ ] RF21: Confirmação do pedido funcionando
- [ ] RF22: Entregador mais próximo selecionado automaticamente ao confirmar pedido
- [ ] RF22: Notificações enviadas para restaurante
- [ ] RF22: Notificações enviadas para entregador
- [ ] RF22: Localização do entregador pode ser atualizada
- [ ] RF22: Pedidos com entregador atribuído são persistidos
- [ ] RF23: Pedido imediato criado corretamente
- [ ] RF23: Pedido agendado criado corretamente
- [ ] RF23: Validação de horário no passado
- [ ] RF23: Formato de data validado

---

## Executar Testes Unitários (Opcional)

Se você tiver Maven instalado:

```bash
# Instalar Maven (se necessário)
brew install maven  # macOS

mvn test -Dtest=NotificationServiceTest
mvn test -Dtest=DeliveryAssignmentServiceTest
mvn test -Dtest=ScheduledOrderTest
# Executar todos os testes
mvn test

# Executar teste específico
mvn test -Dtest=RestaurantServiceTest
mvn test -Dtest=DeliveryServiceTest
mvn test -Dtest=OrderServiceTest
mvn test -Dtest=RestaurantAvailabilityTest
```

---

## Resumo Final

Todos os requisitos funcionais foram implementados e testados:

- **RF01**: Classes, validações e testes completos
- **RF02**: Validação de documentos implementada
- **RF22**: Sistema de notificações e seleção de entregador mais próximo
- **RF23**: Suporte a pedidos agendados e imediatos
- **RF19**: Busca de restaurantes por localização
- **RF20**: Visualização de cardápio e cálculo de taxas
- **RF21**: Criação e confirmação de pedidos

O projeto encontra-se pronto para utilização.
