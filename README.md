# 🚀 campaign-ingest

## 📝 Descrição

Função responsável por consumir dados brutos recebidos pelo Adobe Analytics, transformá-los em um evento padrão (DTO) com campos consistentes, realizar validações de campos obrigatórios, identificar eventos inválidos ou duplicados e enviá-los para a fila RabbitMQ.

## 🛠️ Tecnologias utilizadas

- Spring Cloud Function
- RabbitMQ
- Lombok
- Spring RestDOC
- JavaDoc

## ⚙️ Instalação e execução

Clone o repositório e execute o seguinte comando para compilar o projeto:

```bash
mvn clean install
```

## 💡 Exemplos de uso

```http
POST http://localhost:8081/processMessage
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "Fulano de Tal",
  "dateSimulation": "2025-08-28T10:30",
  "amount": 1234.56
}
```

## 📄 Licença

Este projeto está licenciado sob a licença MIT.

## 📬 Contato

```bash
N/A
```

## 🚢 Deploy

```bash
N/A
```
