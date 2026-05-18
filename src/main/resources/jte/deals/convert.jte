@import ru.mentee.power.crm.model.Lead
@param Lead lead

@template.layout.main(content = @`
    <div class="max-w-md mx-auto mt-8">
        <h1 class="text-2xl font-bold mb-6">Конвертация лида в сделку</h1>
        <div class="bg-white rounded shadow-md p-6">
            <p class="mb-2"><strong>Лид:</strong> ${lead.getFirstName()} (${lead.getEmail()})</p>
            <p class="mb-4"><strong>Компания:</strong> ${lead.getCompany() != null ? lead.getCompany().getName() : "—"}</p>

            <form method="post" action="/deals/convert" class="space-y-4">
                <input type="hidden" name="leadId" value="${lead.getId().toString()}">
                <div>
                    <label class="block text-sm font-medium text-gray-700">Сумма сделки (₽)</label>
                    <input type="number" name="amount" step="0.01" required
                           class="w-full px-3 py-2 border border-gray-300 rounded-md"
                           placeholder="100000.00">
                </div>
                <button type="submit" class="w-full bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
                    Создать сделку
                </button>
            </form>
            <div class="mt-4">
                <a href="/leads" class="text-sm text-gray-600 hover:underline">← Вернуться к лидам</a>
            </div>
        </div>
    </div>
`)