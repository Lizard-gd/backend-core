@param java.util.List<ru.mentee.power.crm.model.Lead> leads
@param java.lang.String currentFilter

@template.layout.main(content = @`
    <div class="bg-white rounded-lg shadow-md p-6">
        <div class="mb-6 flex flex-wrap gap-2">
            <a href="/leads" class="px-4 py-2 rounded ${currentFilter == null ?
"bg-blue-600 text-white" : "bg-gray-200 text-gray-700"} hover:bg-blue-500 hover:text-white transition">
                Все
            </a>
            <a href="/leads?status=NEW" class="px-4 py-2 rounded ${currentFilter == "NEW" ?
"bg-blue-600 text-white" : "bg-gray-200 text-gray-700"} hover:bg-blue-500 hover:text-white transition">
                NEW
            </a>
            <a href="/leads?status=CONTACTED" class="px-4 py-2 rounded ${currentFilter == "CONTACTED" ?
"bg-blue-600 text-white" : "bg-gray-200 text-gray-700"} hover:bg-blue-500 hover:text-white transition">
                CONTACTED
            </a>
            <a href="/leads?status=QUALIFIED" class="px-4 py-2 rounded ${currentFilter == "QUALIFIED" ?
"bg-blue-600 text-white" : "bg-gray-200 text-gray-700"} hover:bg-blue-500 hover:text-white transition">
                QUALIFIED
            </a>
            <a href="/leads/new"
               class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
               >
                + Добавить лида
            </a>
        </div>

        @if(currentFilter != null)
            <p class="text-sm text-gray-600 mb-4">Показаны лиды со статусом: <span class="font-semibold">${currentFilter}</span></p>
        @endif

        <h2 class="text-2xl font-bold mb-4">Lead List</h2>

        <div class="overflow-x-auto">
            <table class="min-w-full bg-white border border-gray-200">
                <thead class="bg-gray-100">
                <tr>
                    <th class="px-4 py-2 text-left">Email</th>
                    <th class="px-4 py-2 text-left">Phone</th>
                    <th class="px-4 py-2 text-left">Company</th>
                    <th class="px-4 py-2 text-left">Status</th>
                    <th class="px-4 py-2 text-left">Действия</th>
                </tr>
                </thead>
                <tbody>
                @for (var lead : leads)
                    <tr class="border-t hover:bg-gray-50">
                        <td class="px-4 py-2">${lead.email()}</td>
                        <td class="px-4 py-2">${lead.phone()}</td>
                        <td class="px-4 py-2">${lead.company()}</td>
                        <td class="px-4 py-2">
                            <span class="px-2 py-1 rounded text-sm ${lead.status().equals("NEW") ? "bg-green-100 text-green-800" : lead.status().equals("QUALIFIED") ? "bg-yellow-100 text-yellow-800" : "bg-blue-100 text-blue-800"}">
                                ${lead.status()}
                            </span>
                        </td>
                        <td class="px-4 py-2">
                            <a href="/leads/${lead.id()}/edit" class="text-blue-600 hover:underline">Редактировать</a>
                            <form method="post" action="/leads/${lead.id()}/delete" class="inline ml-2"
                                  onclick="return confirm('Удалить лида ${lead.email()}?')">
                                <button type="submit" class="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700">Удалить</button>
                            </form>
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </div>
`)