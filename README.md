# 🚖 UberPB – Sistema de Mobilidade Simplificado

## 📌 Sobre o Projeto
O **UberPB** é um sistema acadêmico desenvolvido para a disciplina **Engenharia de Software 2 (2025.2)** da **UEPB**.  
Ele é inspirado no funcionamento do Uber, mas com foco em **projeto, arquitetura e qualidade de código**, não em interfaces gráficas.  

O sistema conta com três tipos de usuários:
- **Passageiro/Cliente** → solicita corridas  
- **Motorista** → aceita corridas e transporta passageiros  
- **Administrador** → gerencia usuários, pagamentos e suporte  

---

## ✅ Requisitos Funcionais
### Cadastro e Autenticação
- RF01: Cadastro de passageiros e motoristas  
- RF02: Validação de documentos e veículo para categorias (UberX, Comfort, Black, etc.)  
- RF03: Login com e-mail  

### Solicitação de Corrida
- RF04: Passageiro informa origem e destino  
- RF05: Cálculo de tempo e preço estimados  
- RF06: Opções de categorias (X, Comfort, Black, Bag, XL)  
- RF07: Notificação a motoristas da categoria  

### Aceite da Corrida
- RF08: Motorista pode aceitar ou recusar  
- RF09: Corrida atribuída ao motorista mais próximo  

### Acompanhamento
- RF10: Passageiro acompanha localização do motorista  
- RF11: Estimativa de chegada atualizada  
- RF12: Motorista visualiza rota otimizada  

### Pagamentos
- RF13: Pagamentos via Cartão, PIX, ou dinheiro  
- RF14: Cálculo do valor (distância, tempo, categoria, tarifa dinâmica)  
- RF15: Emissão de recibo eletrônico  

### Avaliações
- RF16: Passageiros e motoristas podem se avaliar mutuamente  
- RF17: Média das avaliações influencia prioridade  

### Histórico
- RF18: Histórico de corridas filtrável por categoria  
- RF19: Requisito surpresa (definido posteriormente)  

---

## 🛠️ Tecnologias Utilizadas
- **Java** (linguagem principal)  
- **JUnit** (testes unitários – mínimo de 80% de cobertura)  
- **Emma Plugin** (relatórios de cobertura de testes no Eclipse)  
- **Padrões de Projeto (Design Patterns)**  
- **Persistência em disco (arquivos locais)**  
- **Interface de comandos (console)**  