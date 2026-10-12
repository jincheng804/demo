import request from './request'

// Load selector options in bounded requests without silently truncating the first page.
export async function loadAllRows(url, filters = {}) {
  const rows = []
  for (let pageNum = 1; ; pageNum++) {
    const result = await request.get(url, { params: { ...filters, pageNum, pageSize: 500 } })
    const page = result.rows || []
    rows.push(...page)
    if (!page.length || rows.length >= Number(result.total || 0)) return rows
  }
}
