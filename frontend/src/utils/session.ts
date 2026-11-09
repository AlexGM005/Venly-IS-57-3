const PROFILE_ID_KEY = 'venly.profileId';

export function saveSessionProfileId(id: number): void {
  localStorage.setItem(PROFILE_ID_KEY, String(id));
}

export function getSessionProfileId(): number | null {
  const raw = localStorage.getItem(PROFILE_ID_KEY);
  const id = raw ? Number(raw) : NaN;
  return Number.isInteger(id) && id > 0 ? id : null;
}

export function clearSessionProfileId(): void {
  localStorage.removeItem(PROFILE_ID_KEY);
}
