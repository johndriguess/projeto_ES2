# Sistema de Múltiplos Papéis

## Visão Geral

O sistema agora suporta que uma pessoa (identificada por email) tenha múltiplos papéis simultaneamente:

- **Passageiro** (classe `Passenger`)
- **Motorista** (classe `Driver`)
- **Entregador** (classe `Delivery`)
- **Restaurante** (classe `Restaurant`)

## Arquitetura

### Classes Criadas

1. **UserProfile** (`model/UserProfile.java`)
   - Agrega todos os papéis que uma pessoa possui
   - Métodos para verificar quais papéis o usuário tem
   - Métodos para obter cada papel específico

2. **MultiRoleMenu** (`cli/MultiRoleMenu.java`)
   - Menu exibido quando o usuário tem múltiplos papéis
   - Permite escolher qual papel usar na sessão atual
   - Redireciona para o menu específico do papel escolhido

### Fluxo de Login

```
┌─────────────────────────┐
│ Usuário faz login       │
│ (email + senha)         │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────────────────────┐
│ Sistema verifica em todos os repos:     │
│ - UserRepository (Passenger/Driver)     │
│ - DeliveryRepository (Delivery)         │
│ - RestaurantRepository (Restaurant)     │
└───────────┬─────────────────────────────┘
            │
            ▼
    ┌───────┴───────┐
    │               │
    ▼               ▼
┌─────────┐   ┌──────────────┐
│ 1 Papel │   │ >1 Papéis    │
└────┬────┘   └──────┬───────┘
     │               │
     ▼               ▼
Menu Direto    MultiRoleMenu
     │               │
     │               ▼
     │         Escolhe papel
     │               │
     └───────┬───────┘
             ▼
      Menu Específico
```

## Exemplos de Uso

### Exemplo 1: Usuário com 1 papel (Motorista)

```
Email: joao@gmail.com
Senha: 123

Login bem-sucedido! Bem-vindo, João!
Papéis disponíveis: Motorista

=== Menu Motorista - João ===
1 - Visualizar Corridas Disponíveis
2 - Aceitar Corrida
...
```

### Exemplo 2: Usuário com 2 papéis (Motorista + Entregador)

```
Email: maria@gmail.com
Senha: 123

Login bem-sucedido! Bem-vindo, Maria!
Papéis disponíveis: Motorista, Entregador

=== Menu - Maria ===
Você tem múltiplos papéis:
  • Motorista
  • Entregador

Escolha o papel que deseja usar:
1 - Acessar como Motorista
2 - Acessar como Entregador
0 - Sair
>
```

### Exemplo 3: Usuário com 3 papéis (Passageiro + Motorista + Entregador)

```
Email: carlos@gmail.com
Senha: 123

Login bem-sucedido! Bem-vindo, Carlos!
Papéis disponíveis: Passageiro, Motorista, Entregador

=== Menu - Carlos ===
Você tem múltiplos papéis:
  • Passageiro
  • Motorista
  • Entregador

Escolha o papel que deseja usar:
1 - Acessar como Passageiro
2 - Acessar como Motorista
3 - Acessar como Entregador
0 - Sair
> 2

=== Menu Motorista - Carlos ===
Veículo: Honda Civic | Categoria: UberX
1 - Visualizar Corridas Disponíveis
2 - Aceitar Corrida
...
```

## Como Cadastrar Múltiplos Papéis

Para ter múltiplos papéis, cadastre-se em cada funcionalidade usando **o mesmo email**:

### Passo 1: Cadastrar como Motorista

```
=== Menu Principal ===
> 2
Nome: João Silva
Email: joao@gmail.com
...
```

### Passo 2: Cadastrar como Entregador (mesmo email)

```
=== Menu Principal ===
> 3
Nome: João Silva
Email: joao@gmail.com  ← Mesmo email!
...
```

### Passo 3: Login

```
=== Menu Principal ===
> 5
Email: joao@gmail.com
Senha: 123

Login bem-sucedido! Bem-vindo, João Silva!
Papéis disponíveis: Motorista, Entregador

Escolha o papel que deseja usar:
1 - Acessar como Motorista
2 - Acessar como Entregador
0 - Sair
>
```

## Validações

### Email Duplicado por Repositório

- Cada repositório valida se o email já existe **dentro dele**
- Mas **permite** que o mesmo email exista em repositórios diferentes
- Isso possibilita múltiplos papéis

Exemplo:

- ✅ Pode cadastrar `joao@gmail.com` como Motorista no UserRepository
- ✅ Pode cadastrar `joao@gmail.com` como Entregador no DeliveryRepository
- ❌ Não pode cadastrar `joao@gmail.com` duas vezes como Motorista (mesmo repo)

### Senha

- Apenas **User** (Passenger/Driver) têm senha
- **Delivery** e **Restaurant** não têm senha no modelo atual
- No login, a senha é validada apenas no UserRepository
- Se o usuário não existe no UserRepository, apenas verifica existência nos outros repos

## Benefícios do Sistema

1. **Flexibilidade**: Uma pessoa pode exercer múltiplas funções
2. **Conveniência**: Um único login dá acesso a todos os papéis
3. **Organização**: Menus separados mantêm as funcionalidades organizadas
4. **Escolha**: Usuário pode escolher qual papel usar em cada sessão

## Limitações Conhecidas

1. **Senha única**: Se a pessoa for Motorista (tem senha) e Entregador (sem senha), usa a senha do Motorista
2. **Dados duplicados**: Nome e telefone podem estar duplicados nos diferentes cadastros
3. **Sem sincronização automática**: Alterar dados em um papel não atualiza os outros

## Melhorias Futuras (Opcional)

1. Adicionar senha para Delivery e Restaurant
2. Criar tabela única de "Person" com múltiplas "Roles"
3. Sincronizar dados pessoais entre papéis
4. Permitir alternar entre papéis sem fazer logout
5. Dashboard unificado mostrando informações de todos os papéis
