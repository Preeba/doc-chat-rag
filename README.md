# Doc Chat RAG

A Spring Boot 3 WebFlux application for document chat using RAG (Retrieval-Augmented Generation) with LangChain4j, Qdrant vector database, and OpenAI.

## Features

- Spring Boot 3 with WebFlux for reactive programming
- LangChain4j for RAG implementation
- Qdrant vector database for document storage and retrieval
- OpenAI integration for text embeddings and generation
- Java 21 with Lombok for clean code
- gRPC and Protobuf support for Qdrant communication
- Comprehensive integration tests with TestContainers

## Prerequisites

- Java 21
- Gradle 8.x
- OpenAI API key
- Docker (for TestContainers)

## Setup

1. Clone the repository
2. Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Testing

The project includes comprehensive integration tests using TestContainers:

```bash
./gradlew test
```

### Test Features

- **QdrantContainer**: Spins up the latest Qdrant container for testing
- **Mock EmbeddingModel**: Returns fixed vectors for predictable testing
- **RAG Testing**: Verifies that `rag.ask("hello")` returns a non-empty string
- **@Testcontainers**: Uses TestContainers for containerized testing
- **@DynamicPropertySource**: Dynamically configures properties for test containers

### Test Structure

```java
@SpringBootTest
@Testcontainers
class RagServiceIntegrationTest {
    
    @Container
    static GenericContainer<?> qdrantContainer = new GenericContainer<>(
        DockerImageName.parse("qdrant/qdrant:latest")
    );
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("qdrant.host", qdrantContainer::getHost);
        registry.add("qdrant.port", () -> qdrantContainer.getMappedPort(6334));
    }
    
    @MockBean
    private EmbeddingModel embeddingModel;
    
    @Test
    void testRagAskReturnsNonEmptyString() {
        // Test implementation
    }
}
```

## Dependencies

- Spring Boot 3.2.0
- Spring WebFlux
- Spring Boot Actuator
- LangChain4j 0.24.0
- OpenAI Java Client
- gRPC and Protobuf
- TestContainers 1.19.3
- Mockito for testing
- Lombok for reducing boilerplate code

## Project Structure

```
src/
├── main/
│   ├── java/com/example/docchatrag/
│   │   ├── DocChatRagApplication.java
│   │   ├── config/
│   │   │   └── RagConfig.java
│   │   ├── service/
│   │   │   └── RagService.java
│   │   └── store/
│   │       └── QdrantEmbeddingStore.java
│   ├── proto/
│   │   └── (Qdrant .proto files)
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/example/docchatrag/
        └── service/
            └── RagServiceIntegrationTest.java
```

## API Endpoints

### Health Check
- `GET /actuator/health` - Returns application health status

### RAG Operations
- `POST /api/rag/ask` - Ask a question using RAG
- `POST /api/rag/add-document` - Add a document to the knowledge base

## Usage Examples

### Starting the Application

```bash
# Set OpenAI API key
export OPENAI_API_KEY=your-api-key-here

# Run the application
./gradlew bootRun
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests RagServiceIntegrationTest
```

### Building the Project

```bash
# Build the project
./gradlew build

# Build without tests
./gradlew build -x test

# Clean and build
./gradlew clean build
```

## Configuration

The application can be configured via `application.yml` or environment variables:

```yaml
# OpenAI Configuration
openai:
  api-key: ${OPENAI_API_KEY:your-api-key-here}
  model: text-embedding-3-small

# Qdrant Configuration
qdrant:
  host: localhost
  port: 6334
  collection-name: doc_chunks
```

## Development

### Adding New Features

1. Create service classes in `src/main/java/com/example/docchatrag/service/`
2. Add corresponding tests in `src/test/java/com/example/docchatrag/service/`
3. Update configuration in `src/main/resources/application.yml`
4. Update this README with new features

### Code Style

- Use Lombok annotations (`@RequiredArgsConstructor`, `@Slf4j`)
- Follow Spring Boot best practices
- Write comprehensive tests with TestContainers
- Use reactive programming with WebFlux

## Troubleshooting

### Common Issues

1. **Docker not running**: TestContainers requires Docker to be running
2. **OpenAI API key not set**: Ensure `OPENAI_API_KEY` environment variable is set
3. **Port conflicts**: Change ports in `application.yml` if needed

### Logs

Enable debug logging by setting:
```yaml
logging:
  level:
    com.example.docchatrag: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License. 