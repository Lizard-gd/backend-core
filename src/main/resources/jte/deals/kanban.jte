@import ru.mentee.power.crm.model.Deal
@import ru.mentee.power.crm.model.DealStatus
@param java.util.Map<DealStatus, java.util.List<Deal>> dealsByStatus
@param java.lang.String error

@template.layout.main(content = @`
    <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-2xl font-bold">Воронка продаж (Kanban)</h1>
            <a href="/deals" class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Список сделок</a>
        </div>

        @if(error != null)
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                ${error}
            </div>
        @endif

        <div class="grid grid-cols-6 gap-4">
            @for(DealStatus status : DealStatus.values())
                <div class="bg-gray-50 rounded shadow p-4">
                    <h2 class="font-bold text-lg mb-4 text-center">${status}</h2>
                    <div class="space-y-3">
                        @for(Deal deal : dealsByStatus.getOrDefault(status, java.util.List.of()))
                            <div class="bg-white p-3 rounded border border-gray-200 shadow-sm">
                                <p class="font-semibold">${deal.getAmount()} ₽</p>
                                <p class="text-sm text-gray-600 truncate">Lead: ${deal.getLeadId()}</p>
                                <form method="post" action="/deals/${deal.getId()}/transition" class="mt-2">
                                    <select name="newStatus" class="text-sm border rounded p-1 w-full">
                                        @for(DealStatus possible : DealStatus.values())
                                            <option value="${possible}" ?disabled="${deal.getStatus() == possible}">
                                                ${possible}
                                            </option>
                                        @endfor
                                    </select>
                                    <button type="submit" class="mt-2 w-full bg-blue-500 text-white text-sm py-1 rounded hover:bg-blue-600">
                                        Перевести
                                    </button>
                                </form>
                            </div>
                        @endfor
                    </div>
                </div>
            @endfor
        </div>
    </div>
`)