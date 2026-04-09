@param java.util.List<ru.mentee.power.crm.model.Lead> leads

@template.layout.main(content = @`
    <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-bold mb-4">Lead List</h2>

        <div class="overflow-x-auto">
            <table class="min-w-full bg-white border border-gray-200">
                <thead class="bg-gray-100">
                <tr>
                    <th class="px-4 py-2 text-left">Email</th>
                    <th class="px-4 py-2 text-left">Phone</th>
                    <th class="px-4 py-2 text-left">Company</th>
                    <th class="px-4 py-2 text-left">Status</th>
                </tr>
                </thead>
                <tbody>
                @for (var lead : leads)
                    <tr class="border-t hover:bg-gray-50">
                        <td class="px-4 py-2">${lead.email()}</td>
                        <td class="px-4 py-2">${lead.phone()}</td>
                        <td class="px-4 py-2">${lead.company()}</td>
                        <td class="px-4 py-2">
                <span class="px-2 py-1 rounded text-sm
                    @if (lead.status().equals("NEW"))
                      bg-green-100 text-green-800
                      @elseif (lead.status().equals("QUALIFIED"))
                            bg-yellow-100 text-yellow-800
                            @else
                                bg-blue-100 text-blue-800
                            @endif">
                            ${lead.status()}
                            </span>
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </div>
`)