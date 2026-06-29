# REST Controller Code Review Checklist
## Категория 1: API Design (5 проблем)
### 1.1 Неправильные HTTP методы
##### Приоритет: CRITICAL Что искать: POST используется для чтения данных, GET для модификации 
##### Плохо:
```
@PostMapping("/getInvitees") // Глагол в URL + неправильный метод
public List&lt;Invitee&gt; getInvitees() { ... }
```
##### Хорошо:
```
@GetMapping("/invitees") // Существительное + правильный HTTP метод
public ResponseEntity&lt;List&lt;InviteeResponse&gt;&gt; getInvitees() { ... }
```
### 1.2 Неправильные статус коды
#### Приоритет: CRITICAL Что искать: 200 для всех операций, отсутствие 201/204/404
#### Плохо:
```
@PostMapping("/invitees")
public Invitee create(@RequestBody Invitee invitee) {
return service.save(invitee); // Всегда 200 OK
}
```
#### Хорошо:
```
@PostMapping("/invitees")
public ResponseEntity&lt;InviteeResponse&gt; create(@Valid @RequestBody CreateInviteeRequest request) {
InviteeResponse created = service.create(request);
URI location = URI.create("/api/invitees/" + created.id());
return ResponseEntity.created(location).body(created); // 201 Created + Location header
}
```
### 1.3 Плохой naming: глаголы в URL
#### Приоритет: MAJOR Что искать: /getInvitees, /createInvitee, /updateInviteeStatus в URLs
#### Плохо:
```
@GetMapping("/getInvitees") // RPC стиль
@PostMapping("/createInvitee")
```
#### Хорошо:
```
@GetMapping("/invitees") // RESTful стиль
@PostMapping("/invitees")
```
### 1.4 Entity вместо DTO в response
#### Приоритет: CRITICAL (security + coupling) Что искать: Возврат JPA Entity классов напрямую 
#### Плохо:
```
@GetMapping("/invitees/{id}")
public Invitee getById(@PathVariable UUID id) {
return repository.findById(id).orElseThrow(); // Entity с JPA annotations, internal fields
}
```
#### Хорошо:
```
@GetMapping("/invitees/{id}")
public ResponseEntity&lt;InviteeResponse&gt; getById(@PathVariable UUID id) {
Invitee invitee = service.getById(id);
return ResponseEntity.ok(mapper.toResponse(invitee)); // DTO без internal fields
}
```
### 1.5 Нет пагинации для списков
#### Приоритет: MAJOR (performance) Что искать: GET endpoints возвращающие List без параметров page/size 
#### Плохо:
```
@GetMapping("/invitees")
public List&lt;Invitee&gt; getAll() {
return repository.findAll(); // Может вернуть 10,000 записей
}
```
#### Хорошо:
```
@GetMapping("/invitees")
public ResponseEntity&lt;Page&lt;InviteeResponse&gt;&gt; getAll(
@PageableDefault(size = 20) Pageable pageable) {
Page&lt;Invitee&gt; page = repository.findAll(pageable);
return ResponseEntity.ok(page.map(mapper::toResponse));
}
```
## Категория 2: Security (5 проблем)
### 2.1 SQL injection через конкатенацию
#### Приоритет: CRITICAL Что искать: String concatenation в SQL запросах 
#### Плохо:
```
String sql = "SELECT * FROM invitees WHERE email = '" + email + "'";
// Injection: email = "admin' OR '1'='1"
```
#### Хорошо:
```
// Spring Data JPA method
Invitee findByEmail(String email); // Автоматическое экранирование

// Или PreparedStatement
PreparedStatement ps = conn.prepareStatement("SELECT * FROM invitees WHERE email = ?");
ps.setString(1, email);
```
### 2.2 Exposure внутренних полей
#### Приоритет: CRITICAL Что искать: password, internalId, version, createdBy в response 
#### Плохо:
```
@Data
@Entity
public class User {
private UUID id;
private String email;
private String password; // НИКОГДА не должно попасть в response
private String internalSystemId; // Internal field
@Version private Long version; // JPA optimistic locking
}

@GetMapping("/users/{id}")
public User getUser(@PathVariable UUID id) {
return userRepo.findById(id).orElseThrow(); // Вернёт ВСЕ поля включая password
}
```
#### Хорошо:
```
public record UserResponse(UUID id, String email, String firstName) {} // Только публичные поля

@GetMapping("/users/{id}")
public ResponseEntity&lt;UserResponse&gt; getUser(@PathVariable UUID id) {
User user = userService.getById(id);
return ResponseEntity.ok(userMapper.toResponse(user)); // password не попадёт в JSON
}
```
### 2.3 Нет валидации входных данных
#### Приоритет: CRITICAL Что искать: @RequestBody без @Valid, нет Bean Validation аннотаций 
#### Плохо:
```
@PostMapping("/invitees")
public Invitee create(@RequestBody Invitee invitee) { // Нет @Valid
return service.save(invitee); // Любые данные принимаются
}
```
#### Хорошо:
```
public record CreateInviteeRequest(
@NotBlank @Email String email,
@NotBlank @Size(min = 2, max = 50) String firstName
) {}

@PostMapping("/invitees")
public ResponseEntity&lt;InviteeResponse&gt; create(@Valid @RequestBody CreateInviteeRequest request) {
// Spring автоматически валидирует, выбрасывает MethodArgumentNotValidException если ошибка
InviteeResponse created = service.create(request);
return ResponseEntity.created(location).body(created);
}
```
### 2.4 Stack trace в error response
#### Приоритет: CRITICAL Что искать: Exception.printStackTrace() или дефолтный Spring error response с trace 
#### Плохо:
```
@GetMapping("/invitees/{id}")
public Invitee getById(@PathVariable UUID id) {
try {
return repository.findById(id).orElseThrow();
} catch (Exception e) {
e.printStackTrace(); // Stack trace в logs OK, но...
throw e; // Default Spring response включает stack trace в JSON для клиента
}
}
```
#### Хорошо:
```
// GlobalExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity&lt;ProblemDetail&gt; handleNotFound(EntityNotFoundException ex) {
        logger.error("Entity not found", ex); // Full stack trace в server logs
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Resource not found" // Generic message для клиента, NO stack trace
        );
        return ResponseEntity.status(404).body(problem);
    }
}
```
### 2.5 Missing authorization checks
#### Приоритет: CRITICAL Что искать: Отсутствие проверок @PreAuthorize, любой пользователь может удалить чужие данные 
#### Плохо:
```
@DeleteMapping("/invitees/{id}")
public ResponseEntity&lt;Void&gt; delete(@PathVariable UUID id) {
service.delete(id); // Любой авторизованный пользователь может удалить любого invitee
return ResponseEntity.noContent().build();
}
```
#### Хорошо:
```
@DeleteMapping("/invitees/{id}")
@PreAuthorize("hasRole('ADMIN') or @inviteeService.isOwner(#id, authentication.name)")
public ResponseEntity&lt;Void&gt; delete(@PathVariable UUID id) {
service.delete(id); // Только ADMIN или owner может удалить
return ResponseEntity.noContent().build();
}
```
## Категория 3: Error Handling (4 проблемы)
### 3.1 Пустые catch блоки
#### Приоритет: MAJOR Что искать: catch (Exception e) {} или catch блоки с только комментарием
#### Плохо:
```
@GetMapping("/invitees/{id}")
public Invitee getById(@PathVariable UUID id) {
try {
return repository.findById(id).orElseThrow();
} catch (Exception e) {
// TODO: handle
return null; // Клиент получит null вместо error response
}
}
```
#### Хорошо:
```
@GetMapping("/invitees/{id}")
public ResponseEntity&lt;InviteeResponse&gt; getById(@PathVariable UUID id) {
Invitee invitee = service.getById(id); // Service выбросит EntityNotFoundException
return ResponseEntity.ok(mapper.toResponse(invitee));
// GlobalExceptionHandler перехватит exception, вернёт 404 с Problem Details
}
```
### 3.2 500 на бизнес-ошибки вместо 4xx
#### Приоритет: MAJOR Что искать: Бизнес-exceptions (EmailAlreadyExistsException) возвращают 500 
#### Плохо:
```
@PostMapping("/invitees")
public Invitee create(@RequestBody Invitee invitee) {
if (repository.existsByEmail(invitee.getEmail())) {
throw new RuntimeException("Email exists"); // Default Spring: 500 Internal Server Error
}
return repository.save(invitee);
}
```
#### Хорошо:
```
// Custom exception
public class EmailAlreadyExistsException extends RuntimeException {
public EmailAlreadyExistsException(String email) {
super("Email already exists: " + email);
}
}

// В Service
if (repository.existsByEmail(request.email())) {
throw new EmailAlreadyExistsException(request.email());
}

// GlobalExceptionHandler
@ExceptionHandler(EmailAlreadyExistsException.class)
public ResponseEntity&lt;ProblemDetail&gt; handleEmailExists(EmailAlreadyExistsException ex) {
ProblemDetail problem = ProblemDetail.forStatusAndDetail(
HttpStatus.CONFLICT, // 409 Conflict (бизнес-ошибка, не server error)
ex.getMessage()
);
return ResponseEntity.status(409).body(problem);
}
```
### 3.3 Generic error messages без деталей
#### Приоритет: MINOR Что искать: "Error occurred", "Something went wrong" без context 
#### Плохо:
```
{
"error": "Error occurred" // Что именно? Какое поле невалидно?
}
```
#### Хорошо:
```
{
"type": "/errors/validation",
"title": "Validation Error",
"status": 400,
"detail": "Request validation failed for 2 fields",
"instance": "/api/invitees",
"errors": {
"email": "Email is required and must be valid",
"firstName": "First name must be between 2 and 50 characters"
}
}
```
### 3.4 Нет логирования ошибок
#### Приоритет: MAJOR Что искать: Exceptions обрабатываются, но не логируются
#### Плохо:
```
@ExceptionHandler(Exception.class)
public ResponseEntity&lt;ErrorResponse&gt; handleGeneric(Exception ex) {
return ResponseEntity.status(500).body(new ErrorResponse("Internal error"));
// Exception потерян, нет способа debug в production
}
```
#### Хорошо:
```
@ExceptionHandler(Exception.class)
public ResponseEntity&lt;ProblemDetail&gt; handleGeneric(Exception ex, HttpServletRequest request) {
logger.error("Unexpected error for request: {} {}", request.getMethod(), request.getRequestURI(), ex);
// Full stack trace в logs с context (HTTP method, URI, parameters)

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred" // Generic message для клиента
    );
    return ResponseEntity.status(500).body(problem);
}
```
## Категория 4: Code Quality (4 проблемы)
### 4.1 Бизнес-логика в контроллере
#### Приоритет: MAJOR (violates SRP) Что искать: if/else бизнес-правила, расчёты, обращения к нескольким repositories 
#### Плохо:
```
@PostMapping("/invitees")
public ResponseEntity&lt;Invitee&gt; create(@RequestBody Invitee invitee) {
// Бизнес-логика в контроллере
if (repository.existsByEmail(invitee.getEmail())) {
throw new EmailAlreadyExistsException(invitee.getEmail());
}

    if (invitee.getFirstName().length() < 2) {
        throw new ValidationException("Name too short");
    }

    invitee.setCreatedAt(Instant.now());
    invitee.setStatus(InviteeStatus.NEW);

    Invitee saved = repository.save(invitee);
    emailService.sendWelcomeEmail(saved.getEmail());

    return ResponseEntity.created(location).body(saved);
}
```
#### Хорошо:
```
@PostMapping("/invitees")
public ResponseEntity&lt;InviteeResponse&gt; create(@Valid @RequestBody CreateInviteeRequest request) {
InviteeResponse created = inviteeService.create(request); // ВСЯ бизнес-логика в Service
URI location = URI.create("/api/invitees/" + created.id());
return ResponseEntity.created(location).body(created);
}

// InviteeService
public InviteeResponse create(CreateInviteeRequest request) {
validateEmailUnique(request.email());
Invitee invitee = buildNewInvitee(request);
Invitee saved = repository.save(invitee);
emailService.sendWelcomeEmail(saved.getEmail());
return mapper.toResponse(saved);
}
```
### 4.2 Дублирование кода
#### Приоритет: MAJOR (violates DRY) Что искать: Одинаковый try-catch в каждом методе, повторяющийся маппинг DTO 
#### Плохо:
```
@GetMapping("/invitees/{id}")
public Invitee getById(@PathVariable UUID id) {
try {
return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Invitee not found"));
} catch (EntityNotFoundException e) {
// Дублируется в каждом методе
return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
}
}

@GetMapping("/deals/{id}")
public Deal getDealById(@PathVariable UUID id) {
try {
return dealRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Deal not found"));
} catch (EntityNotFoundException e) {
// То же самое error handling
return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
}
}
```
#### Хорошо:
```
// Контроллеры просто выбрасывают exceptions
@GetMapping("/invitees/{id}")
public ResponseEntity&lt;InviteeResponse&gt; getById(@PathVariable UUID id) {
Invitee invitee = service.getById(id); // Выбросит EntityNotFoundException если не найден
return ResponseEntity.ok(mapper.toResponse(invitee));
}

// GlobalExceptionHandler обрабатывает для ВСЕХ контроллеров
@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity&lt;ProblemDetail&gt; handleNotFound(EntityNotFoundException ex) {
ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
return ResponseEntity.status(404).body(problem);
}
}
```
### 4.3 God Controller: слишком много методов
#### Приоритет: MINOR (violates Cohesion) Что искать: Контроллер с 20+ методами для несвязанных операций
#### Плохо:
```
@RestController
@RequestMapping("/invitees")
public class InviteeController {
// CRUD для Invitee
@GetMapping public List&lt;Invitee&gt; getAll() {}
@GetMapping("/{id}") public Invitee getById() {}
@PostMapping public Invitee create() {}
@PutMapping("/{id}") public Invitee update() {}
@DeleteMapping("/{id}") public void delete() {}

    // Конверсия в Deal
    @PostMapping("/{id}/convert") public Deal convertToDeal() {}

    // Email уведомления
    @PostMapping("/{id}/send-welcome") public void sendWelcome() {}
    @PostMapping("/{id}/send-reminder") public void sendReminder() {}

    // Отчёты
    @GetMapping("/report/monthly") public Report getMonthly() {}
    @GetMapping("/report/by-status") public Report getByStatus() {}

    // Импорт/экспорт
    @PostMapping("/import") public void importCsv() {}
    @GetMapping("/export") public byte[] exportExcel() {}
    // ... ещё 15 методов
}
```
#### Хорошо:
```
// Разделение на несколько контроллеров по bounded contexts
@RestController
@RequestMapping("/invitees")
public class InviteeController { // Только CRUD
@GetMapping public ResponseEntity&lt;Page&lt;InviteeResponse&gt;&gt; getAll() {}
@GetMapping("/{id}") public ResponseEntity&lt;InviteeResponse&gt; getById() {}
@PostMapping public ResponseEntity&lt;InviteeResponse&gt; create() {}
@PutMapping("/{id}") public ResponseEntity&lt;InviteeResponse&gt; update() {}
@DeleteMapping("/{id}") public ResponseEntity&lt;Void&gt; delete() {}
}

@RestController
@RequestMapping("/invitees/{inviteeId}/conversion")
public class InviteeConversionController { // Конверсия в Deal
@PostMapping public ResponseEntity&lt;DealResponse&gt; convertToDeal() {}
}

@RestController
@RequestMapping("/invitees/{inviteeId}/notifications")
public class InviteeNotificationController { // Email уведомления
@PostMapping("/welcome") public ResponseEntity&lt;Void&gt; sendWelcome() {}
@PostMapping("/reminder") public ResponseEntity&lt;Void&gt; sendReminder() {}
}

@RestController
@RequestMapping("/reports/invitees")
public class InviteeReportController { // Отчёты
@GetMapping("/monthly") public ResponseEntity&lt;ReportResponse&gt; getMonthly() {}
@GetMapping("/by-status") public ResponseEntity&lt;ReportResponse&gt; getByStatus() {}
}
```
### 4.4 Hardcoded values
#### Приоритет: MINOR Что искать: Magic numbers, hardcoded URLs, roles в коде 
#### Плохо:
```
@GetMapping("/invitees")
public List&lt;Invitee&gt; getAll(@RequestParam(defaultValue = "20") int size) { // Hardcoded 20
// ...
}

@PreAuthorize("hasRole('ROLE_ADMIN')") // Hardcoded role name
public void delete(@PathVariable UUID id) {}

String apiUrl = "https://api.example.com/send-email"; // Hardcoded URL
```
#### Хорошо:
```
// application.yml
app:
pagination:
default-page-size: 20
max-page-size: 100
roles:
admin: ROLE_ADMIN
external-api:
email-service-url: https://api.example.com/send-email

// Configuration class
@ConfigurationProperties(prefix = "app")
public class AppProperties {
private Pagination pagination;
private Roles roles;
private ExternalApi externalApi;

    public record Pagination(int defaultPageSize, int maxPageSize) {}
    public record Roles(String admin) {}
    public record ExternalApi(String emailServiceUrl) {}
}

// В контроллере
@GetMapping("/invitees")
public ResponseEntity&lt;Page&lt;InviteeResponse&gt;&gt; getAll(
@PageableDefault(size = 20) Pageable pageable) { // Можно переопределить в application.yml
// Spring автоматически использует spring.data.web.pageable.default-page-size
}

@PreAuthorize("hasRole(@appProperties.roles().admin())")
public ResponseEntity&lt;Void&gt; delete(@PathVariable UUID id) {}
```
<!-- TODO: Использовать этот чек-лист при анализе каждого REST контроллера:
    1. Пройти по всем категориям: API Design → Security → Error Handling → Code Quality
    2. Для каждой проблемы искать в коде признаки (URL, аннотации, исключения, DTO)
    3. Фиксировать найденные проблемы в отчёте (CODE_REVIEW_REPORT.md) с указанием строки, категории и приоритета
-->