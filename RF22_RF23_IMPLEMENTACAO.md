# Implementação RF22 e RF23 - Relatório

## Requisitos Implementados

### RF22 - Notificar o restaurante e o entregador mais próximo

#### Classes Criadas:

1. **Notification.java** - Modelo de notificação
   - Atributos: id, recipientId, recipientType, message, timestamp, read
   - Métodos para marcar como lida

2. **NotificationService.java** - Serviço de notificações
   - `notifyRestaurant()` - Notifica restaurante sobre novo pedido
   - `notifyDelivery()` - Notifica entregador sobre atribuição
   - `getNotificationsByRecipient()` - Busca notificações por destinatário
   - `getUnreadNotificationsByRecipient()` - Busca notificações não lidas
   - `markAsRead()` - Marca notificação como lida
   - `markAllAsRead()` - Marca todas como lidas

3. **DeliveryAssignmentService.java** - Serviço de atribuição de entregadores
   - `findNearestAvailableDelivery()` - Encontra entregador mais próximo
   - `assignDeliveryToOrder()` - Atribui entregador ao pedido e notifica
   - `findAvailableDeliveriesInRadius()` - Busca entregadores em raio específico
   - `calculateDistance()` - Calcula distância entre entregador e localização

#### Alterações em Classes Existentes:

- **Order.java**:
  - Adicionado campo `assignedDeliveryId`
  - Adicionado `implements Serializable` para persistência
- **Delivery.java**: Adicionado campo `currentLocation` (Location)
- **DeliveryRepository.java**:
  - Adicionado método `findById()`
  - Implementada persistência em arquivo
- **OrderRepository.java**:
  - Implementada persistência em arquivo (`data/orders.db`)
  - Adicionado método `update()` para atualizar pedidos
- **OrderService.java**: Integração com NotificationService para notificar restaurante na confirmação
- **Main.java**:
  - Atribuição automática de entregador ao confirmar pedido
  - Remoção da opção manual de atribuir entregador
  - Persistência automática de pedidos com entregador

---

### RF23 - Pedidos agendados ou imediatos

#### Classes Criadas:

1. **OrderType.java** - Enum para tipo de pedido
   - IMEDIATO
   - AGENDADO

#### Alterações em Classes Existentes:

- **Order.java**:
  - Adicionado campo `orderType` (OrderType)
  - Adicionado campo `scheduledTime` (LocalDateTime)
  - Novo construtor para pedidos agendados
  - Métodos: `isScheduled()`, `isImmediate()`

- **OrderService.java**:
  - `createScheduledOrder()` - Cria pedido agendado com validações
  - `createImmediateOrder()` - Cria pedido imediato
  - `getScheduledOrders()` - Busca todos os pedidos agendados
  - `getImmediateOrders()` - Busca todos os pedidos imediatos
  - `findById()` - Busca pedido por ID

---

## Testes Criados

### Testes RF22:

1. **DeliveryAssignmentServiceTest.java** - 7 testes
   - Encontrar entregador mais próximo
   - Validação quando não há entregador disponível
   - Não selecionar entregador inativo
   - Atribuir entregador ao pedido
   - Buscar entregadores em raio específico
   - Calcular distância corretamente

2. **NotificationServiceTest.java** - 8 testes
   - Notificar restaurante
   - Notificar entregador
   - Buscar notificações por destinatário
   - Buscar notificações não lidas
   - Marcar notificação como lida
   - Marcar todas como lidas
   - Limpar notificações

### Testes RF23:

1. **ScheduledOrderTest.java** - 8 testes
   - Criar pedido agendado
   - Criar pedido imediato
   - Não permitir pedido agendado no passado
   - Validar horário obrigatório para pedidos agendados
   - Buscar pedidos agendados
   - Buscar pedidos imediatos
   - Agendar pedido para horário específico
   - Calcular preço corretamente para pedidos agendados

---

## Novas Funcionalidades no Main.java

### Opções do Menu:

- **Opção 20**: Cadastrar Entregador (RF02)
- **Opção 21**: Cadastrar Restaurante (RF01)
- **Opção 22**: Listar Restaurantes Disponíveis (RF19)
- **Opção 23**: Ver Cardápio e Detalhes do Restaurante (RF20)

- **Opção 24**: Fazer Pedido Imediato (RF21 + RF23 + RF22)
  - Lista restaurantes disponíveis por localização
  - Permite seleção por número
  - Cria pedido imediato
  - **Atribui entregador automaticamente ao confirmar**
  - Persiste pedido com entregador no banco de dados

- **Opção 25**: Fazer Pedido Agendado (RF23 + RF22)
  - Lista restaurantes disponíveis por localização
  - Permite seleção por número
  - Solicita data/hora de agendamento
  - Valida horário futuro
  - **Atribui entregador automaticamente ao confirmar**
  - Persiste pedido com entregador no banco de dados

- **Opção 26**: Ver Notificações
  - Exibe notificações por destinatário
  - Mostra status (LIDA/NÃO LIDA)
  - Permite marcar todas como lidas

- **Opção 27**: Atualizar Localização do Entregador
  - Atualiza localização para cálculo de proximidade
  - Necessário antes de fazer pedidos para atribuição automática

### Função Auxiliar Adicionada:

- **selectRestaurantFromList()**:
  - Solicita localização do usuário
  - Lista restaurantes disponíveis no raio especificado
  - Exibe nome, endereço, distância e quantidade de itens
  - Permite seleção por número ou cancelamento
  - Reutilizada em pedidos imediatos e agendados

### Fluxo de Uso:

#### Para Pedidos Imediatos ou Agendados:

1. Selecionar opção 24 (imediato) ou 25 (agendado)
2. Informar sua localização (endereço, latitude/longitude, raio)
3. Sistema lista restaurantes disponíveis na região
4. Escolher restaurante pelo número
5. Sistema exibe cardápio do restaurante
6. Selecionar itens do cardápio
7. Informar distância e desconto
8. Para pedido agendado: informar data/hora (formato: dd/MM/yyyy HH:mm)
9. Confirmar pedido
10. **Sistema automaticamente:**
    - Busca o entregador mais próximo disponível
    - Atribui o entregador ao pedido
    - Envia notificações para restaurante e entregador
    - Persiste o pedido completo no banco de dados
    - Exibe informações do entregador atribuído

#### Para Atualizar Localização do Entregador:

1. Selecionar opção 27
2. Informar email do entregador
3. Informar nova localização (endereço, latitude, longitude)
4. Sistema atualiza e persiste a localização

**Nota**: A localização do entregador deve ser atualizada antes de fazer pedidos para que a atribuição automática funcione corretamente.

#### Para Ver Notificações:

1. Selecionar opção 26
2. Informar ID do destinatário (restaurante ou entregador)
3. Sistema exibe todas as notificações
4. Opção de marcar todas como lidas

---

## Validações Implementadas

### RF22:

- Entregador deve estar ativo
- Entregador deve ter status APROVADO
- Entregador deve ter localização cadastrada
- Localização do restaurante é obrigatória
- Atribuição automática ao confirmar pedido
- Pedido com entregador é persistido automaticamente

### RF23:

- Horário agendado não pode ser no passado
- Horário agendado é obrigatório para pedidos do tipo AGENDADO
- Pedido deve conter pelo menos um item
- Formato de data/hora deve ser válido (dd/MM/yyyy HH:mm)

---

## Como Testar

### Teste RF22 - Atribuição Automática de Entregador:

1. Cadastrar restaurante com localização (opção 21)
2. Cadastrar entregador (opção 20)
3. Atualizar localização do entregador (opção 27)
4. Criar pedido (opção 24 ou 25)
5. Confirmar pedido quando solicitado
6. **Sistema atribui entregador automaticamente**
7. Ver notificações do entregador e restaurante (opção 26)
8. Verificar que pedido foi persistido com entregador atribuído

**Resultado Esperado**:

- Entregador mais próximo é selecionado automaticamente
- Notificações enviadas para restaurante e entregador
- Pedido salvo no banco de dados com entregador atribuído
- Informações do entregador exibidas ao confirmar pedido

### Teste RF23 - Pedido Agendado:

1. Cadastrar restaurante com localização
2. Criar pedido agendado (opção 25)
3. Informar localização para buscar restaurantes
4. Escolher restaurante da lista
5. Agendar para daqui 2 horas
6. Verificar que o tipo é AGENDADO
7. Verificar horário agendado no resumo
8. Tentar criar pedido agendado no passado (deve falhar)

---

## Execução

```bash
cd /Users/vitor/Desktop/projeto_ES2
java -cp target/classes cli.Main
```

---

## Testes Unitários

Para executar os testes:

```bash
# Todos os testes
mvn test

# Testes específicos RF22
mvn test -Dtest=NotificationServiceTest
mvn test -Dtest=DeliveryAssignmentServiceTest

# Testes específicos RF23
mvn test -Dtest=ScheduledOrderTest
```

---

## Resumo

Todas as funcionalidades dos requisitos RF22 e RF23 foram implementadas com sucesso:

- Sistema de notificações funcional
- Seleção automática do entregador mais próximo
- Suporte completo a pedidos agendados e imediatos
- 23 testes unitários criados
- Interface CLI completa para todas as operações
- Validações robustas em todos os fluxos

O projeto está pronto para uso e todos os testes passam com sucesso.
