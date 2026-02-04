import { useState } from 'react';
import { format, subMonths } from 'date-fns';

export function PeriodPicker({ onSearch, loading, disabled }) {
  const [startDate, setStartDate] = useState(format(subMonths(new Date(), 3), 'yyyy-MM-dd'));
  const [endDate, setEndDate] = useState(format(new Date(), 'yyyy-MM-dd'));

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch({ startDate, endDate });
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-4 p-4 bg-white/5 rounded-lg border border-white/10">
      <div className="flex flex-col gap-1">
        <label htmlFor="startDate" className="text-sm text-gray-400">시작일</label>
        <input
          id="startDate"
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          className="px-3 py-2 rounded bg-white/10 border border-white/20 text-sm"
        />
      </div>
      <div className="flex flex-col gap-1">
        <label htmlFor="endDate" className="text-sm text-gray-400">종료일</label>
        <input
          id="endDate"
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          className="px-3 py-2 rounded bg-white/10 border border-white/20 text-sm"
        />
      </div>
      <button
        type="submit"
        disabled={loading || disabled}
        className="px-4 py-2 rounded bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-sm font-medium"
      >
        {loading ? '조회 중…' : '조회'}
      </button>
    </form>
  );
}
