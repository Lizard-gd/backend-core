@param ru.mentee.power.crm.model.Lead lead
@param org.springframework.validation.BindingResult errors = null

@template.layout.main(
content = @`
<div class="max-w-md mx-auto mt-8">
    <h1 class="text-2xl font-bold mb-6">Добавить нового лида</h1>

    <form action="/leads" method="post" class="space-y-4">
        <!-- FirstName -->
        <div>
            <label class="block text-sm font-medium text-gray-700">
                Имя
            </label>
            <input type="text"
                   name="firstName"
                   required
                   value="${lead.getFirstName()}"
                   class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 ${errors != null
&& errors.hasFieldErrors("firstName") ? "border-red-500" : ""}"
                   placeholder="Моё имя" />
            @if(errors != null && errors.hasFieldErrors("firstName"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("firstName").getDefaultMessage()}</p>
            @endif
        </div>
        <!-- Email -->
        <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                Email
            </label>
            <input
                type="email"
                id="email"
                name="email"
                value="${lead.getEmail()}"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500
${errors != null && errors.hasFieldErrors("email") ? "border-red-500" : ""}"
                placeholder="example@company.com"
                />
            @if(errors != null && errors.hasFieldErrors("email"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("email").getDefaultMessage()}</p>
            @endif
        </div>
        <!-- Phone -->
        <div>
            <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">
                Телефон
            </label>
            <input
                    type="text"
                    id="phone"
                    name="phone"
                    value="${lead.getPhone()}"
                    required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500
${errors != null && errors.hasFieldErrors("phone") ? "border-red-500" : ""}"
                    placeholder="+123456789"
            />
            @if(errors != null && errors.hasFieldErrors("phone"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("phone").getDefaultMessage()}</p>
            @endif
        </div>
        <!-- Company -->
        <div>
            <label for="company" class="block text-sm font-medium text-gray-700 mb-1">
                Компания
            </label>
            <input
                    type="text"
                    id="company"
                    name="company"
                    value="${lead.getCompany()}"
                    required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500
${errors != null && errors.hasFieldErrors("company") ? "border-red-500" : ""}"
                    placeholder="Test Corp"
            />
            @if(errors != null && errors.hasFieldErrors("company"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("company").getDefaultMessage()}</p>
            @endif
        </div>
        <!-- Status -->
        <div>
            <label for="status" class="block text-sm font-medium text-gray-700 mb-1">
                Статус
            </label>
            <select
                    id="status"
                    name="status"
                    required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500
${errors != null && errors.hasFieldErrors("status") ? "border-red-500" : ""}"
                    >
                <option value="NEW" selected?="${lead.getStatus().equals("NEW")}">NEW</option>
                <option value="CONTACTED" selected?="${lead.getStatus().equals("CONTACTED")}">CONTACTED</option>
                <option value="QUALIFIED" selected?="${lead.getStatus().equals("QUALIFIED")}">QUALIFIED</option>
                <option value="LOST" selected?="${lead.getStatus().equals("LOST")}">LOST</option>
            </select>
            @if(errors != null && errors.hasFieldErrors("status"))
                <p class="text-red-600 text-sm mt-1">${errors.getFieldError("status").getDefaultMessage()}</p>
            @endif
        </div>
        <!-- Submit Button-->
        <button
            type="submit"
            class="w-full bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 focus:ring-2 focus:ring-blue-500"
            >
            Создать лида
        </button>

        <!-- Cancel Link -->
        <a
            href="/leads"
            class="block text-center text-sm text-gray-600 hover:text-gray-900"
            >
            Отмена
        </a>
    </form>
</div>
`
)