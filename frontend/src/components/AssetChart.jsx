import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts';

export function AssetChart({ data, startDate, endDate, cumulativeReturnPct, cumulativeChange }) {
  if (!data || data.length === 0) {
    return (
      <div className="p-6 text-center text-gray-500">
        기간을 선택한 뒤 조회하면 월별 자산 변동 차트가 표시됩니다.
      </div>
    );
  }

  const chartData = data.map((m) => ({
    name: m.yearMonth,
    자산: m.endAsset != null ? Number(m.endAsset) : 0,
  }));

  return (
    <div className="p-4">
      {(cumulativeReturnPct != null || cumulativeChange != null) && (
        <div className="mb-4 p-3 rounded bg-white/5 border border-white/10 text-sm">
          <span className="text-gray-400">
            {startDate} ~ {endDate}
          </span>
          {cumulativeReturnPct != null && (
            <span className="ml-4 font-medium">
              기간 수익률: <span className="text-green-400">{cumulativeReturnPct}</span>
            </span>
          )}
          {cumulativeChange != null && (
            <span className="ml-4 text-gray-300">
              기간 총 수익금: {cumulativeChange >= 0 ? '+' : ''}{Number(cumulativeChange).toLocaleString()}원
            </span>
          )}
        </div>
      )}
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 10, right: 20, left: 10, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
            <XAxis dataKey="name" stroke="#9ca3af" fontSize={12} />
            <YAxis stroke="#9ca3af" fontSize={12} tickFormatter={(v) => (v >= 10000 ? `${(v / 10000).toFixed(0)}만` : v)} />
            <Tooltip
              formatter={(value) => [value != null ? Number(value).toLocaleString() + '원' : '-', '추정자산']}
              contentStyle={{ background: '#1f2937', border: '1px solid #374151' }}
            />
            <Bar dataKey="자산" fill="#3b82f6" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
