import { query, getMemoryDb } from '../../lib/db';
import { getAuthenticatedUser } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || identity.role !== 'admin') {
    return res.status(403).json({ message: 'Forbidden. Admin privileges required.' });
  }

  try {
    const sql = `
      SELECT w.*, u.username, u.email_id
      FROM worker w
      JOIN user u ON u.worker_id = w.worker_id
    `;
    let workers = await query(sql);

    // Support memory DB fallback structure if needed
    if (workers.length === 0) {
      const db = getMemoryDb();
      workers = db.worker.map(w => {
        const u = db.user.find(usr => usr.worker_id === w.worker_id);
        return {
          ...w,
          username: u ? u.username : 'Applicant',
          email_id: u ? u.email_id : 'applicant@test.com'
        };
      });
    }

    return res.status(200).json(workers);
  } catch (err) {
    console.error('Fetch workers admin error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
