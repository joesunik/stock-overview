const ACCOUNTS_KEY = 'stock-overview-accounts';
const DEFAULT_ACCOUNT_KEY = 'stock-overview-default-account';

export function loadAccounts() {
  try {
    const raw = localStorage.getItem(ACCOUNTS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveAccounts(accounts) {
  localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts));
}

export function loadDefaultAccount() {
  try {
    return localStorage.getItem(DEFAULT_ACCOUNT_KEY) || '';
  } catch {
    return '';
  }
}

export function saveDefaultAccount(acctNo) {
  localStorage.setItem(DEFAULT_ACCOUNT_KEY, acctNo ?? '');
}
