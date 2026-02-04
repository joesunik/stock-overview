import { useState } from 'react';

export function AccountSelector({
  accounts,
  defaultAccount,
  selectedAccount,
  onSelectAccount,
  onAddAccount,
  onSetDefault,
  onRefresh,
  onRefreshForeignAndFutures,
  addingLoading,
  refreshLoading,
  refreshForeignLoading,
  hasSearchParams,
}) {
  const [newAcct, setNewAcct] = useState('');
  const [showAdd, setShowAdd] = useState(false);

  const handleAdd = (e) => {
    e.preventDefault();
    const trimmed = newAcct.trim();
    if (!trimmed) return;
    if (accounts.some((a) => a.acct_no === trimmed)) {
      setNewAcct('');
      setShowAdd(false);
      return;
    }
    onAddAccount(trimmed);
    setNewAcct('');
    setShowAdd(false);
  };

  const handleSetDefault = () => {
    const selected = accounts.find((a) => a.acct_no === selectedAccount);
    if (selected?.id != null) onSetDefault(selected.id);
  };

  if (accounts.length === 0) {
    return (
      <div className="p-4 bg-white/5 rounded-lg border border-white/10">
        <p className="text-sm text-gray-400 mb-3">계좌를 추가해 주세요.</p>
        <form onSubmit={handleAdd} className="flex flex-wrap items-end gap-3">
          <div className="flex flex-col gap-1">
            <label htmlFor="newAcct" className="text-sm text-gray-400">계좌번호</label>
            <input
              id="newAcct"
              type="text"
              value={newAcct}
              onChange={(e) => setNewAcct(e.target.value)}
              placeholder="예: 12345678-01"
              className="px-3 py-2 rounded bg-white/10 border border-white/20 text-sm w-44"
              disabled={addingLoading}
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 rounded bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-sm font-medium"
            disabled={addingLoading}
          >
            {addingLoading ? '추가 중…' : '추가'}
          </button>
        </form>
      </div>
    );
  }

  return (
    <div className="p-4 bg-white/5 rounded-lg border border-white/10">
      <div className="flex flex-wrap items-end gap-4">
        <div className="flex flex-col gap-1">
          <label htmlFor="accountSelect" className="text-sm text-gray-400">계좌 선택</label>
          <select
            id="accountSelect"
            value={selectedAccount}
            onChange={(e) => onSelectAccount(e.target.value)}
            className="px-3 py-2 rounded bg-white/10 border border-white/20 text-sm min-w-[180px]"
          >
            {accounts.map((acct) => (
              <option key={acct.id} value={acct.acct_no}>
                {acct.acct_no} {acct.acct_no === defaultAccount ? '(기본)' : ''}
              </option>
            ))}
          </select>
        </div>
        <button
          type="button"
          onClick={handleSetDefault}
          disabled={selectedAccount === defaultAccount}
          className="px-4 py-2 rounded bg-white/10 hover:bg-white/15 disabled:opacity-50 text-sm font-medium"
        >
          기본 계좌로 설정
        </button>
        {onRefresh && (
          <button
            type="button"
            onClick={onRefresh}
            disabled={refreshLoading}
            className="px-4 py-2 rounded bg-white/10 hover:bg-white/15 disabled:opacity-50 text-sm font-medium"
          >
            {refreshLoading ? '새로고침 중…' : '새로고침'}
          </button>
        )}
        {onRefreshForeignAndFutures && hasSearchParams && (
          <button
            type="button"
            onClick={onRefreshForeignAndFutures}
            disabled={refreshForeignLoading}
            className="px-4 py-2 rounded bg-amber-600 hover:bg-amber-700 disabled:opacity-50 text-sm font-medium"
          >
            {refreshForeignLoading ? '해외/선물 갱신 중…' : '해외/선물 갱신'}
          </button>
        )}
        {showAdd ? (
          <form onSubmit={handleAdd} className="flex flex-wrap items-end gap-3">
            <div className="flex flex-col gap-1">
              <label htmlFor="newAcctInline" className="text-sm text-gray-400">계좌번호</label>
              <input
                id="newAcctInline"
                type="text"
                value={newAcct}
                onChange={(e) => setNewAcct(e.target.value)}
                placeholder="예: 12345678-01"
                className="px-3 py-2 rounded bg-white/10 border border-white/20 text-sm w-44"
                disabled={addingLoading}
              />
            </div>
            <button
              type="submit"
              className="px-4 py-2 rounded bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-sm font-medium"
              disabled={addingLoading}
            >
              {addingLoading ? '추가 중…' : '추가'}
            </button>
            <button
              type="button"
              onClick={() => { setShowAdd(false); setNewAcct(''); }}
              className="px-4 py-2 rounded bg-white/10 text-sm"
              disabled={addingLoading}
            >
              취소
            </button>
          </form>
        ) : (
          <button
            type="button"
            onClick={() => setShowAdd(true)}
            className="px-4 py-2 rounded bg-white/10 hover:bg-white/15 text-sm font-medium"
            disabled={addingLoading}
          >
            계좌 추가
          </button>
        )}
      </div>
    </div>
  );
}
