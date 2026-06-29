## Issue №1: [Неправильные HTTP метод]

### **Категория:**
- API Design

### **Приоритет:**
- CRITICAL

### **Местоположение:**
- InviteeController.java, строка [30], метод [getInvitees()]

#### **Что плохо:**
```
@PostMapping("/getInvitees")
```

#### **Почему плохо:**
[POST используется для чтения данных, GET для модификации]

#### **Как исправить:**
```
@GetMapping("/Invitees")
```

---

## Issue №2: [Entity вместо DTO в response]

### **Категория:**
- API Design

### **Приоритет:**
- CRITICAL

### **Местоположение:**
- InviteeController.java, строка [36], метод [getById()]

#### **Что плохо:**
```
public Invitee getById(@PathVariable UUID id) {
return repository.findById(id).orElse(null);
```

#### **Почему плохо:**
[Entity с JPA annotations, internal fields]

#### **Как исправить:**
```
@GetMapping("/invitees/{id}")
public ResponseEntity&lt;InviteeResponse&gt; getById(@PathVariable UUID id) {
Invitee invitee = service.getById(id);
return ResponseEntity.ok(mapper.toResponse(invitee)); // DTO без internal fields
}
```

---

## Issue №3: [SQL injection через конкатенацию]

### **Категория:** 
- Security

### **Приоритет:** 
- CRITICAL

### **Местоположение:** 
- InviteeController.java, строка [48], метод [create()]

#### **Что плохо:**
```
String sql = "SELECT COUNT(*) FROM invitees WHERE email = '" + email + "'";
```
#### **Почему плохо:**
[Injection: email = "admin' OR '1'='1"]

#### **Как исправить:**
```
PreparedStatement ps = conn.prepareStatement("SELECT * FROM invitees WHERE email = ?");
ps.setString(1, email);
```

---

## Issue №4: [Нет валидации входных данных]

### **Категория:**
- Security

### **Приоритет:**
- CRITICAL

### **Местоположение:**
- InviteeController.java, строка [43], метод [create()]

#### **Что плохо:**
```
public Invitee create(@RequestBody Map<String, Object> params)
```

#### **Почему плохо:**
[@RequestBody без @Valid, нет Bean Validation]

#### **Как исправить:**
```
public Invitee create(@Valid @RequestBody Map<String, Object> params)
```

---

## Issue №5: [Missing authorization checks]

### **Категория:**
- Security

### **Приоритет:**
- CRITICAL

### **Местоположение:**
- InviteeController.java, строка [62], метод [delete()]

#### **Что плохо:**
```
@DeleteMapping("/invitees/{id}")
    public Invitee delete(@PathVariable UUID id) {
```
#### **Почему плохо:**
[Отсутствие проверок @PreAuthorize, любой пользователь может удалить чужие данные]

#### **Как исправить:**
```
@DeleteMapping("/invitees/{id}")
@PreAuthorize("hasRole('ADMIN') or @inviteeService.isOwner(#id, authentication.name)")
```

---

## Issue №6: [Бизнес-логика в контроллере]

### **Категория:**
- Code Quality

### **Приоритет:**
- MAJOR

### **Местоположение:**
- InviteeController.java, строка [78], метод [updateStatus()]

#### **Что плохо:**
```
if (status.equals("ACTIVE") || status.equals("INACTIVE")) {
                invitee.setStatus(status);
```
#### **Почему плохо:**
[Бизнес-логика в контроллере]

#### **Как исправить:**
```
InviteeResponse update = inviteeService.update(status);
```

---
## Issue №7: [Пустые catch блоки]

### **Категория:**
- Error Handling

### **Приоритет:**
- MAJOR

### **Местоположение:**
- InviteeController.java, строка [85], метод [updateStatus()]

#### **Что плохо:**
```
} catch (Exception e) {
            // Пустой catch
            return null;
        }
```
#### **Почему плохо:**
[Клиент получит null вместо error response]

#### **Как исправить:**
```
@GetMapping("/invitees/{id}")
public ResponseEntity&lt;InviteeResponse&gt; getById(@PathVariable UUID id) {
    Invitee invitee = service.getById(id); // Service выбросит EntityNotFoundException
    return ResponseEntity.ok(mapper.toResponse(invitee));
```

---

## Issue №8: [Неправильный статус-код при создании]

### **Категория:**
- API Design

### **Приоритет:**
- CRITICAL

### **Местоположение:**
- InviteeController.java, строка [43], метод [create()]

#### **Что плохо:**
```
Invitee invitee = new Invitee();
        invitee.setId(UUID.randomUUID());
        invitee.setEmail(email);
        invitee.setFirstName(firstName);
        invitee.setCreatedAt(Instant.now());

        return repository.save(invitee);
```
#### **Почему плохо:**
[Возвращает Invitee напрямую, Spring по умолчанию отдаёт 200 OK. Для создания ресурса правильный статус – 201 Created с заголовком Location.]

#### **Как исправить:**
```
@PostMapping("/invitees")
public ResponseEntity&lt;InviteeResponse&gt; create(@Valid @RequestBody CreateInviteeRequest request) {
    InviteeResponse created = service.create(request);
    URI location = URI.create("/api/invitees/" + created.id());
    return ResponseEntity.created(location).body(created); // 201 Created + Location header
}
```

---

## Issue №9: [Отсутствие пагинации для списка]

### **Категория:**
- API Design

### **Приоритет:**
- MAJOR

### **Местоположение:**
- InviteeController.java, строка [31], метод [getInvitees()]

#### **Что плохо:**
```
public List<Invitee> getInvitees() {
        return repository.findAll();
    }
```
#### **Почему плохо:**
[Возвращает List<Invitee> без пагинации – при большом количестве записей это ударит по производительности.]

#### **Как исправить:**
```
@GetMapping("/invitees")
public ResponseEntity&lt;Page&lt;InviteeResponse&gt;&gt; getAll(
    @PageableDefault(size = 20) Pageable pageable) {
    Page&lt;Invitee&gt; page = repository.findAll(pageable);
    return ResponseEntity.ok(page.map(mapper::toResponse));
}
```

---

## Issue №10: [Использование Map<String, Object> вместо DTO]

### **Категория:**
- Security + Code Quality

### **Приоритет:**
- MAJOR

### **Местоположение:**
- InviteeController.java, строка [43], метод [create()]

#### **Что плохо:**
```
public Invitee create(@RequestBody Map<String, Object> params)
```
#### **Почему плохо:**
[Принимает сырой Map, нет типизации, нет валидации, сложно читать и поддерживать.]

#### **Как исправить:**
```
Создать DTO CreateInviteeRequest с полями email, firstName и аннотациями @NotBlank, @Email и т.д.

```