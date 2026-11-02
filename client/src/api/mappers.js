// Maps API payloads (Java field names) to the shapes the existing pages use.

export function mapUser(user) {
  if (!user) return null;
  const role = (user.role || "STUDENT").toLowerCase();
  return {
    id: String(user.id),
    name: user.name ?? "",
    email: user.email ?? "",
    role,
    status: (user.status || "ACTIVE").toLowerCase(),
    rollNo: user.rollNo ?? "",
    branch: user.branch ?? "",
    batch: user.batch ?? "",
    cgpa: user.cgpa === null || user.cgpa === undefined ? "" : Number(user.cgpa),
    backlogs: user.backlogs ?? 0,
    department: user.department ?? "",
    createdAt: user.createdAt ?? null,
  };
}

export function mapCompany(a) {
  if (!a) return null;
  return {
    id: String(a.id),
    companyName: a.companyName ?? "",
    role: a.roleTitle ?? "",
    details: a.details ?? "",
    ctc: a.ctc === null || a.ctc === undefined ? 0 : Number(a.ctc),
    location: a.location ?? "",
    jdFileName: a.jdFileName ?? "",
    lastDate: a.lastDate ?? "",
    testDate: a.testDate ?? "",
    interviewDate: a.interviewDate ?? "",
    venue: a.venue ?? "",
    testLink: a.testLink ?? "",
    eligibility: {
      branches: a.eligibility?.branches ?? [],
      minCgpa:
        a.eligibility?.minCgpa === null || a.eligibility?.minCgpa === undefined
          ? 0
          : Number(a.eligibility.minCgpa),
      maxBacklogs: a.eligibility?.maxBacklogs ?? 0,
      batch: a.eligibility?.batch ?? "",
    },
    rounds: (a.rounds ?? []).map((r) => ({ name: r.name, date: r.date ?? "" })),
    isArchived: Boolean(a.archived),
    eligible: Boolean(a.eligible),
    applied: Boolean(a.applied),
    bookmarked: Boolean(a.bookmarked),
    result: { posted: Boolean(a.resultPosted) },
  };
}

const NOTIFICATION_TYPES = {
  ANNOUNCEMENT: "announcement",
  REMINDER: "reminder",
  RESULT: "result",
  SCHEDULE: "update",
  APPLICATION: "update",
  ACCOUNT: "update",
};

export function mapNotification(n) {
  return {
    id: n.id,
    message: n.message ?? "",
    time: n.createdAt ?? "",
    read: Boolean(n.read),
    type: NOTIFICATION_TYPES[n.type] ?? "update",
  };
}

// Payload for POST/PUT /api/announcements
export function toAnnouncementPayload(form) {
  return {
    companyName: form.companyName,
    roleTitle: form.role,
    details: form.details,
    ctc: Number(form.ctc) || 0,
    location: form.location,
    lastDate: form.lastDate,
    testDate: form.testDate || null,
    interviewDate: form.interviewDate || null,
    venue: form.venue,
    testLink: form.testLink,
    eligibility: {
      branches: form.branches,
      minCgpa: Number(form.minCgpa) || 0,
      maxBacklogs: Number(form.maxBacklogs) || 0,
      batch: form.batch || null,
    },
    rounds: (form.rounds ?? [])
      .filter((r) => r.name.trim())
      .map((r) => ({ name: r.name, date: r.date || null })),
  };
}
