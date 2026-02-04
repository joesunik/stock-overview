export function TransactionTable({ items }) {
  if (!items || items.length === 0) {
    return (
      <div className="p-6 text-center text-gray-500">
        거래/입출금 내역이 없거나 아직 연동되지 않았습니다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto p-4">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b border-white/20">
            <th className="text-left py-2 px-2 text-gray-400">일자</th>
            <th className="text-left py-2 px-2 text-gray-400">구분</th>
            <th className="text-right py-2 px-2 text-gray-400">금액</th>
            <th className="text-left py-2 px-2 text-gray-400">비고</th>
          </tr>
        </thead>
        <tbody>
          {items.map((row, i) => (
            <tr key={i} className="border-b border-white/10 hover:bg-white/5">
              <td className="py-2 px-2">{row.date}</td>
              <td className="py-2 px-2">{row.type || '-'}</td>
              <td className="py-2 px-2 text-right">{row.amount != null ? Number(row.amount).toLocaleString() : '-'}</td>
              <td className="py-2 px-2 text-gray-500">{row.remark || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
