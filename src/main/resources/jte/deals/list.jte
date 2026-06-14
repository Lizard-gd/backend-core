@import ru.mentee.power.crm.model.Deal
@param java.util.List<Deal> deals

@template.layout.main(content = @`
    <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex justify-between items-center mb-6">
            <a href="/leads" class="text-blue-600 hover:underline">← Назад к лидам</a>
            <h1 class="text-2xl font-bold">Сделки CRM</h1>
            <a href="/deals/kanban" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Воронка продаж</a>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full bg-white border border-gray-200">
                <thead class="bg-gray-100">
                <tr>
                    <th class="px-4 py-2 text-left">ID</th>
                    <th class="px-4 py-2 text-left">Lead ID</th>
                    <th class="px-4 py-2 text-left">Сумма</th>
                    <th class="px-4 py-2 text-left">Статус</th>
                    <th class="px-4 py-2 text-left">Дата создания</th>
                </tr>
                </thead>
                <tbody>
                @for(Deal deal : deals)
                    @var statusClass = ""
                    @if(deal.getStatus().toString().equals("LOST"))
                        statusClass = "bg-red-100 text-red-800"
                    @elseif (deal.getStatus().toString().equals("NEW"))
                        statusClass = "bg-green-100 text-green-800"
                    @elseif (deal.getStatus().toString().equals("QUALIFIED"))
                        statusClass = "bg-yellow-100 text-yellow-800"
                    @elseif (deal.getStatus().toString().equals("PAUSE"))
                        statusClass = "bg-gray-300 text-gray-800"
                    @else
                        statusClass = "bg-blue-100 text-blue-800"
                    @endif
                    <tr class="border-t hover:bg-gray-50">
                        <td class="px-4 py-2">${deal.getId()}</td>
                        <td class="px-4 py-2">${deal.getLeadId()}</td>
                        <td class="px-4 py-2">${deal.getAmount()} ₽</td>
                        <td class="px-4 py-2">
                            <span class="px-2 py-1 rounded text-sm ${deal.getStatus().getCssClass()}">
                                ${deal.getStatus()}
                            </span>
                        </td>
                        <td class="px-4 py-2">
                            ${deal.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </div>
`)