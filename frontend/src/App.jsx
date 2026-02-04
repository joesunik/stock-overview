import { useState, useEffect, useCallback } from 'react';
import { AccountSelector } from './components/AccountSelector';
import { PeriodPicker } from './components/PeriodPicker';
import { AssetChart } from './components/AssetChart';
import {
  fetchAccounts,
  addAccount as apiAddAccount,
  setDefaultAccount as apiSetDefault,
  refreshAccount as apiRefreshAccount,
  refreshForeignAndFuturesData as apiRefreshForeignAndFutures,
  fetchMonthlySummary,
} from './api/accountApi';

function App() {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState('');
  const [loading, setLoading] = useState(false);
  const [addingLoading, setAddingLoading] = useState(false);
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [refreshForeignLoading, setRefreshForeignLoading] = useState(false);
  const [error, setError] = useState(null);
  const [monthlySummary, setMonthlySummary] = useState(null);
  const [searchParams, setSearchParams] = useState(null);

  const loadAccounts = useCallback(async () => {
    try {
      const list = await fetchAccounts();
      setAccounts(Array.isArray(list) ? list : []);
      if (Array.isArray(list) && list.length > 0) {
        const defaultOne = list.find((a) => a.is_default);
        const acctNo = defaultOne ? defaultOne.acct_no : list[0].acct_no;
        setSelectedAccount((prev) => (list.some((a) => a.acct_no === prev) ? prev : acctNo));
      } else {
        setSelectedAccount('');
      }
    } catch (e) {
      setError(e.message || '계좌 목록 조회 실패');
      setAccounts([]);
      setSelectedAccount('');
    }
  }, []);

  useEffect(() => {
    loadAccounts();
  }, [loadAccounts]);

  const defaultAccount = accounts.find((a) => a.is_default)?.acct_no || '';

  const handleAddAccount = async (acctNo) => {
    const trimmed = acctNo.trim();
    if (!trimmed) return;
    if (accounts.some((a) => a.acct_no === trimmed)) return;
    setError(null);
    setAddingLoading(true);
    try {
      await apiAddAccount(trimmed);
      await loadAccounts();
      setSelectedAccount(trimmed);
    } catch (e) {
      setError(e.message || '계좌 추가 실패');
    } finally {
      setAddingLoading(false);
    }
  };

  const handleSetDefault = async (accountId) => {
    if (accountId == null) return;
    setError(null);
    try {
      await apiSetDefault(accountId);
      await loadAccounts();
    } catch (e) {
      setError(e.message || '기본 계좌 설정 실패');
    }
  };

  const handleRefresh = async () => {
    const acctNo = selectedAccount;
    if (!acctNo) return;
    setError(null);
    setRefreshLoading(true);
    try {
      await apiRefreshAccount(acctNo);
      if (searchParams) {
        const summaryRes = await fetchMonthlySummary(searchParams.startDate, searchParams.endDate, acctNo);
        setMonthlySummary(summaryRes);
      }
    } catch (e) {
      setError(e.message || '새로고침 실패');
    } finally {
      setRefreshLoading(false);
    }
  };

  const handleRefreshForeignAndFutures = async () => {
    const acctNo = selectedAccount;
    if (!acctNo || !searchParams) return;
    setError(null);
    setRefreshForeignLoading(true);
    try {
      await apiRefreshForeignAndFutures(acctNo, searchParams.startDate, searchParams.endDate);
      const summaryRes = await fetchMonthlySummary(searchParams.startDate, searchParams.endDate, acctNo);
      setMonthlySummary(summaryRes);
    } catch (e) {
      setError(e.message || '해외주식/선물 갱신 실패');
    } finally {
      setRefreshForeignLoading(false);
    }
  };

  const handleSearch = async ({ startDate, endDate }) => {
    setError(null);
    setSearchParams({ startDate, endDate });
    setLoading(true);
    const acctNo = selectedAccount || undefined;
    try {
      const summaryRes = await fetchMonthlySummary(startDate, endDate, acctNo);
      setMonthlySummary(summaryRes);
    } catch (e) {
      setError(e.message || '조회 실패');
      setMonthlySummary(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen p-6 box-border">
      <h1 className="text-xl font-semibold mb-6">키움 계좌 자산 변동</h1>
      <div className="mb-6">
        <AccountSelector
          accounts={accounts}
          defaultAccount={defaultAccount}
          selectedAccount={selectedAccount}
          onSelectAccount={setSelectedAccount}
          onAddAccount={handleAddAccount}
          onSetDefault={handleSetDefault}
          onRefresh={handleRefresh}
          onRefreshForeignAndFutures={handleRefreshForeignAndFutures}
          addingLoading={addingLoading}
          refreshLoading={refreshLoading}
          refreshForeignLoading={refreshForeignLoading}
          hasSearchParams={searchParams != null}
        />
      </div>
      <PeriodPicker onSearch={handleSearch} loading={loading} disabled={accounts.length === 0} />
      {error && (
        <div className="mt-4 p-3 rounded bg-red-500/20 border border-red-500/50 text-red-300 text-sm">
          {error}
        </div>
      )}
      {addingLoading && (
        <div className="mt-4 p-3 rounded bg-blue-500/20 text-blue-300 text-sm">
          2015년부터 데이터 수집 중… (완료될 때까지 잠시 기다려 주세요)
        </div>
      )}
      {monthlySummary && (
        <section className="mt-6 rounded-lg border border-white/10 overflow-hidden">
          <h2 className="px-4 py-2 bg-white/5 text-sm font-medium text-gray-300">월별 자산 변동</h2>
          <AssetChart
            data={monthlySummary.monthly_summaries}
            startDate={monthlySummary.start_date}
            endDate={monthlySummary.end_date}
            cumulativeReturnPct={monthlySummary.cumulative_return_pct}
            cumulativeChange={monthlySummary.cumulative_change}
          />
        </section>
      )}
    </div>
  );
}

export default App;
