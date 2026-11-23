# Budgeting · API de orçamento com assistente por voz

API REST de controle de gastos feita com **Spring Boot 4** e **Spring AI 2**, em que dá para registrar e consultar gastos **falando**. O áudio é transcrito, o modelo decide qual função da aplicação chamar (Tool Calling) e a resposta volta em áudio.

Projeto do desafio **"Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot"**, do bootcamp de Java com IA da [DIO](https://www.dio.me). Parti do projeto base da trilha ([dio-spring-boot-learning-track / 05-spring-ai](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai)) e fui evoluindo a partir dele.

## Como funciona

```
 áudio (.m4a, .mp3, .webm...)
   │
   ▼
 TranscriptionModel (whisper-1) ──► "gastei 80 reais no mercado"
   │
   ▼
 ChatClient + tools ──► o modelo escolhe a ferramenta certa
   │                    persist-transaction(description, 8000, GROCERIES)
   ▼
 Casos de uso (mesmos da API REST) ──► banco MySQL
   │
   ▼
 "Pronto, registrei 80 reais em mercado."
   │
   ▼
 TextToSpeechModel (gpt-4o-mini-tts) ──► resposta.mp3
```

A ideia principal é que **a IA não acessa o banco direto**. Ela só pode fazer o que os casos de uso permitem, com as mesmas regras de negócio da API REST. Se o modelo tentar salvar um gasto de 0 reais, quem barra é o código Java, não o prompt.

## O que eu evoluí em relação à base

| Base da DIO | Minha versão |
| --- | --- |
| 2 tools: registrar e listar por categoria | 5 tools: registrar, listar por categoria, **últimos gastos**, **resumo do período** e **apagar** |
| Valor salvo em centavos, mas devolvido como se fosse reais (R$ 45,90 aparecia como 4590.0) | Conversão de centavos para reais corrigida e testada |
| 3 categorias | 9 categorias, com nome em português para a resposta falada |
| Sem data nas transações | Data de criação, respeitando o fuso de São Paulo |
| Fluxo de IA dentro do controller | Fluxo isolado em `VoiceAssistant` e `BudgetAssistant`; controller só recebe e responde |
| Sem validação | Regras no domínio (valor > 0, descrição obrigatória) + Bean Validation no REST + validação do áudio **antes** de chamar a OpenAI |
| Erros viravam 500 | Erros padronizados com Problem Details (400, 404, 413, 502) |
| Só endpoint de áudio | Também um endpoint de **texto**, para testar o Tool Calling sem gastar com transcrição e síntese |
| — | Totais calculados em Java (o modelo só lê e explica o resultado, não faz conta) |
| — | Página web simples para gravar pelo navegador e ouvir a resposta |
| Testes dependiam da chave da OpenAI | Testes unitários rodam sem chave, com H2 e repositório em memória |

## Estrutura

```
src/main/java/dio/budgeting
├── domain/            Transaction, Category, TransactionRepository, exceções de negócio
├── application/       Casos de uso (também expostos como @Tool para a IA)
│   ├── input/ output/
└── infrastructure/
    ├── ai/            AiConfig, BudgetAssistant, VoiceAssistant, AudioUploadValidator
    ├── http/          TransactionController, AssistantController, ApiExceptionHandler
    ├── persistence/   Entidade JPA e adaptador do repositório
    └── config/        Clock (fuso horário)
```

## Como rodar

Pré-requisitos: **Java 25**, **Docker** rodando e uma chave da OpenAI.

```bash
export OPENAI_API_KEY="sua_chave_aqui"
./gradlew bootRun
```

O MySQL sobe sozinho pelo `compose.yml` (suporte a Docker Compose do Spring Boot). Depois é só abrir **http://localhost:8080** para usar a página com o microfone.

> Se você já rodou a versão base antes, recrie o banco para pegar as colunas novas: `docker compose down -v`.

## Endpoints

### Assistente

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/transactions/ai` | Envia áudio (`multipart`, campo `file`) e recebe a resposta em MP3. A transcrição e o texto da resposta vêm nos headers `X-Transcription` e `X-Assistant-Reply` |
| POST | `/transactions/ai/text` | Mesmo assistente, só que com texto: `{"message": "quanto gastei este mês?"}` |

```bash
# Por voz (usa um dos áudios de exemplo do projeto)
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  -D - -o resposta.mp3

# Por texto
curl -X POST http://localhost:8080/transactions/ai/text \
  -H "Content-Type: application/json" \
  -d '{"message": "gastei 32 reais e 50 centavos de uber ontem"}'
```

Comandos que dá para testar:
- "Gastei 80 reais no mercado"
- "Paguei 45 e 90 na farmácia"
- "Quanto eu gastei este mês?"
- "Quanto gastei com restaurante na semana passada?"
- "Quais foram meus últimos gastos?"
- "Apaga o último gasto, lancei errado"

### REST

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/transactions` | Cria um gasto: `{"description": "mercado", "category": "GROCERIES", "amountInCents": 8000}` |
| GET | `/transactions?limit=10` | Últimos gastos |
| GET | `/transactions/{category}` | Gastos de uma categoria |
| GET | `/transactions/summary?startDate=2026-11-01&endDate=2026-11-30` | Total do período e total por categoria (sem datas = mês atual) |
| DELETE | `/transactions/{id}` | Apaga um gasto |

Categorias: `GROCERIES`, `PHARMA`, `AUTO`, `RESTAURANT`, `TRANSPORT`, `BILLS`, `LEISURE`, `EDUCATION`, `OTHER`.

## Testes

```bash
./gradlew test
```

- **Sem chave**: rodam os testes de domínio, casos de uso (resumo, fuso horário, conversão de centavos, exclusão) e validação de áudio, com H2 em memória.
- **Com `OPENAI_API_KEY`**: rodam também os testes `*IT`, que chamam a OpenAI de verdade (transcrição dos áudios de exemplo, síntese de voz e Tool Calling ponta a ponta em `BudgetAssistantIT`).

## Tecnologias

Java 25 · Spring Boot 4 · Spring AI 2 (ChatClient, Tool Calling, Transcription, Text-to-Speech) · OpenAI (gpt-4o-mini, whisper-1, gpt-4o-mini-tts) · Spring Data JPA · MySQL · Docker Compose · JUnit 5 · H2

## Próximos passos

- Autenticação, para cada pessoa ver só os próprios gastos
- Memória de conversa (`ChatMemory`) para perguntas encadeadas, tipo "e no mês passado?"
- Confirmação por voz antes de apagar um gasto
- Rodar com modelo local (Ollama) para não depender de API paga

---

Feito por **Nathan Mota** · [LinkedIn](https://www.linkedin.com/in/nathan-mota-ba596438a/) · [GitHub](https://github.com/nathanmotadev)
