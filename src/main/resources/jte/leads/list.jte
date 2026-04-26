@param java.util.List<ru.mentee.power.crm.model.Lead> leads
@param java.lang.String currentFilter
@param java.lang.String search
@param java.lang.String status
@param java.time.LocalDateTime fromDateTime
@param java.time.LocalDateTime toDateTime

@template.layout.main(content = @`
    <div class="mb-6 bg-white shadow rounded-lg p-4">
        <form method="get" action="/leads" class="flex flex-wrap gap-4 items-end">
            <div class="flex-1 min-w-[200px]">
                <label class="block text-sm font-medium text-gray-700">Поиск по имени или email</label>
                @if(search != null)
                    <input type="text" name="search" value="${search}" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
                @else
                    <input type="text" name="search" value="" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
                @endif
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700">Дата с (включительно)</label>
                <input type="datetime-local" name="fromDateTime"
                       value="${fromDateTime != null ? fromDateTime.toString().substring(0, 16) : ""}"
                       class="mt-1 block rounded-md border-gray-300 shadow-sm">
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700">Дата по (включительно)</label>
                <input type="datetime-local" name="toDateTime"
                       value="${toDateTime != null ? toDateTime.toString().substring(0, 16) : ""}"
                       class="mt-1 block rounded-md border-gray-300 shadow-sm">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700">Статус</label>
                <select name="status" class="mt-1 block rounded-md border-gray-300 shadow-sm">
                    @if(status == null || "".equals(status))
                        <option value="" selected>Все статусы</option>
                    @else
                        <option value="">Все статусы</option>
                    @endif
                    @if("NEW".equals(status))
                        <option value="NEW" selected>NEW</option>
                    @else
                        <option value="NEW">NEW</option>
                    @endif
                    @if("CONTACTED".equals(status))
                        <option value="CONTACTED" selected>CONTACTED</option>
                    @else
                        <option value="CONTACTED">CONTACTED</option>
                    @endif
                    @if("QUALIFIED".equals(status))
                        <option value="QUALIFIED" selected>QUALIFIED</option>
                    @else
                        <option value="QUALIFIED">QUALIFIED</option>
                    @endif
                    @if("LOST".equals(status))
                        <option value="LOST" selected>LOST</option>
                    @else
                        <option value="LOST">LOST</option>
                    @endif
                </select>
            </div>
            <div class="flex gap-2">
                <button type="submit" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700">Применить</button>
                <a href="/leads" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">Сбросить</a>
            </div>
        </form>
    </div>
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
            <a href="/leads?status=LOST" class="px-4 py-2 rounded ${currentFilter == "LOST" ?
"bg-blue-600 text-white" : "bg-gray-200 text-gray-700"} hover:bg-blue-500 hover:text-white transition">
                LOST
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
                    <th class="px-4 py-2 text-left">Имя</th>
                    <th class="px-4 py-2 text-left">Email</th>
                    <th class="px-4 py-2 text-left">Телефон</th>
                    <th class="px-4 py-2 text-left">Компания</th>
                    <th class="px-4 py-2 text-left">Статус</th>
                    <th class="px-4 py-2 text-left">Дата создания</th>
                    <th class="px-4 py-2 text-left">Действия</th>
                </tr>
                </thead>
                <tbody>
                @for (var lead : leads)
                    <tr class="border-t hover:bg-gray-50">
                        <td class="px-4 py-2">${lead.firstName()}</td>
                        <td class="px-4 py-2">${lead.email()}</td>
                        <td class="px-4 py-2">${lead.phone()}</td>
                        <td class="px-4 py-2">${lead.company()}</td>
                        <td class="px-4 py-2">
                            <span class="px-2 py-1 rounded text-sm ${lead.status().equals("LOST") ? "bg-red-100 text-red-800" : lead.status().equals("NEW") ? "bg-green-100 text-green-800" : lead.status().equals("QUALIFIED") ? "bg-yellow-100 text-yellow-800" : "bg-blue-100 text-blue-800"}">
                                ${lead.status()}
                            </span>
                        </td>
                        <td class="px-4 py-2">
                            ${lead.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}
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