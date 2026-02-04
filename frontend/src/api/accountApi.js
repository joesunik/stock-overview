const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// 날짜를 YYYY-MM-DD 형식으로 정규화 (Date 객체 또는 문자열 모두 처리)
function normalizeDate(date) {
  if (!date) return null;
  if (typeof date === 'string') {
    // 이미 문자열인 경우 YYYY-MM-DD 형식 검증
    if (/^\d{4}-\d{2}-\d{2}$/.test(date)) return date;
    // ISO 형식 또는 다른 형식은 파싱 후 포맷
    const parsed = new Date(date);
    if (!isNaN(parsed)) {
      return parsed.toISOString().split('T')[0];
    }
  } else if (date instanceof Date) {
    return date.toISOString().split('T')[0];
  }
  return null;
}

function buildParams(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v != null && v !== '') {
      search.set(k, String(v));
    }
  });
  const q = search.toString();
  return q ? `?${q}` : '';
}

async function parseResponse(res) {
  const text = await res.text();
  if (!res.ok) {
    let msg = text || `HTTP ${res.status}`;
    try {
      const json = JSON.parse(text);
      if (json.error) msg = json.error;
      if (json.message) msg = json.message;
    } catch (_) {}
    console.error('❌ API 오류:', {
      status: res.status,
      statusText: res.statusText,
      message: msg,
      url: res.url
    });
    throw new Error(msg);
  }
  return text ? JSON.parse(text) : null;
}

export async function fetchMonthlySummary(startDate, endDate, acctNo = '') {
  const normalizedStart = normalizeDate(startDate);
  const normalizedEnd = normalizeDate(endDate);

  if (!normalizedStart || !normalizedEnd) {
    throw new Error('유효하지 않은 날짜 형식입니다.');
  }

  const url = `${API_BASE}/api/account/monthly-summary${buildParams({
    startDate: normalizedStart,
    endDate: normalizedEnd,
    ...(acctNo && { acctNo }),
  })}`;
  const res = await fetch(url);
  return parseResponse(res);
}

export async function fetchTransactions(startDate, endDate, acctNo = '') {
  const normalizedStart = normalizeDate(startDate);
  const normalizedEnd = normalizeDate(endDate);

  if (!normalizedStart || !normalizedEnd) {
    throw new Error('유효하지 않은 날짜 형식입니다.');
  }

  const url = `${API_BASE}/api/account/transactions${buildParams({
    startDate: normalizedStart,
    endDate: normalizedEnd,
    ...(acctNo && { acctNo }),
  })}`;
  const res = await fetch(url);
  return parseResponse(res);
}

export async function fetchDepositWithdrawalSummary(startDate, endDate, acctNo = '') {
  const normalizedStart = normalizeDate(startDate);
  const normalizedEnd = normalizeDate(endDate);

  if (!normalizedStart || !normalizedEnd) {
    throw new Error('유효하지 않은 날짜 형식입니다.');
  }

  const url = `${API_BASE}/api/account/deposit-withdrawal-summary${buildParams({
    startDate: normalizedStart,
    endDate: normalizedEnd,
    ...(acctNo && { acctNo }),
  })}`;
  const res = await fetch(url);
  return parseResponse(res);
}

export async function fetchAccounts() {
  const url = `${API_BASE}/api/accounts`;
  const res = await fetch(url);
  return parseResponse(res);
}

export async function addAccount(acctNo) {
  const url = `${API_BASE}/api/accounts`;
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ acctNo: acctNo.trim() }),
  });
  return parseResponse(res);
}

export async function setDefaultAccount(accountId) {
  const url = `${API_BASE}/api/accounts/${accountId}/default`;
  const res = await fetch(url, { method: 'PATCH' });
  if (!res.ok) {
    const text = await res.text();
    let msg = text;
    try {
      const json = JSON.parse(text);
      if (json.error) msg = json.error;
    } catch (_) {}
    throw new Error(msg);
  }
}

export async function refreshAccount(acctNo) {
  const url = `${API_BASE}/api/account/refresh${buildParams({ acctNo })}`;
  const res = await fetch(url, { method: 'POST' });
  return parseResponse(res);
}

export async function refreshForeignAndFuturesData(acctNo, startDate, endDate) {
  // 입력값 검증
  if (!acctNo || !startDate || !endDate) {
    throw new Error('계좌번호, 시작일, 종료일은 필수입니다.');
  }

  // 날짜 정규화
  const normalizedStart = normalizeDate(startDate);
  const normalizedEnd = normalizeDate(endDate);

  if (!normalizedStart || !normalizedEnd) {
    throw new Error('유효하지 않은 날짜 형식입니다.');
  }

  const url = `${API_BASE}/api/account/refresh-foreign-futures${buildParams({
    acctNo,
    startDate: normalizedStart,
    endDate: normalizedEnd,
  })}`;

  console.debug('🔄 해외주식/선물 갱신 요청', { url, acctNo, startDate: normalizedStart, endDate: normalizedEnd });
  const res = await fetch(url, { method: 'POST' });
  return parseResponse(res);
}
