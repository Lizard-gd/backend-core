@import ru.mentee.power.crm.model.Lead
@param Lead lead
@param org.springframework.validation.BindingResult errors = null

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Редактирование лида</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
<div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Редактирование лида</h1>
    <form method="post" action="/leads/${lead.id()}" class="bg-white p-6 rounded shadow-md">
        <input type="hidden" name="id" value="${lead.id()}">

        <div>
            <label class="block text-sm font-medium text-gray-700">Имя</label>
            <input type="text" name="firstName" value="${lead.firstName()}"
                   class="w-full px-3 py-2 border rounded ${errors != null
&& errors.hasFieldErrors("firstName") ? "border-red-500" : "border-gray-300"}">
            @if(errors != null && errors.hasFieldErrors("firstName"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("firstName").getDefaultMessage()}</p>
            @endif
        </div>

        <div class="mb-4">
            <label class="block text-gray-700 font-bold mb-2">Email</label>
            <input type="email" name="email" value="${lead.email()}"
                   class="w-full px-3 py-2 border rounded ${errors != null
&& errors.hasFieldErrors("email") ? "border-red-500" : "border-gray-300"}">
            @if(errors != null && errors.hasFieldErrors("email"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("email").getDefaultMessage()}</p>
            @endif
        </div>

        <div class="mb-4">
            <label class="block text-gray-700 font-bold mb-2">Телефон</label>
            <input type="tel" name="phone" value="${lead.phone()}"
                   class="w-full px-3 py-2 border rounded ${errors != null
&& errors.hasFieldErrors("phone") ? "border-red-500" : "border-gray-300"}">
            @if(errors != null && errors.hasFieldErrors("phone"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("phone").getDefaultMessage()}</p>
            @endif
        </div>

        <div class="mb-4">
            <label class="block text-gray-700 font-bold mb-2">Компания</label>
            <input type="text" name="company" value="${lead.company()}"
                   class="w-full px-3 py-2 border rounded ${errors != null
&& errors.hasFieldErrors("company") ? "border-red-500" : "border-gray-300"}">
            @if(errors != null && errors.hasFieldErrors("company"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("company").getDefaultMessage()}</p>
            @endif
        </div>

        <div class="mb-4">
            <label class="block text-gray-700 font-bold mb-2">Статус</label>
            <select name="status" class="w-full px-3 py-2 border rounded ${errors != null
&& errors.hasFieldErrors("status") ? "border-red-500" : "border-gray-300"}">
                <option value="NEW" selected?="${lead.status().equals("NEW")}">NEW</option>
                <option value="CONTACTED" selected?="${lead.status().equals("CONTACTED")}">CONTACTED</option>
                <option value="QUALIFIED" selected?="${lead.status().equals("QUALIFIED")}">QUALIFIED</option>
                <option value="LOST" selected?="${lead.status().equals("LOST")}">LOST</option>
            </select>
            @if(errors != null && errors.hasFieldErrors("status"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("status").getDefaultMessage()}</p>
            @endif
        </div>

        <div class="flex gap-2">
            <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Сохранить</button>
            <a href="/leads" class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Отмена</a>
        </div>
    </form>
</div>
</body>
</html>