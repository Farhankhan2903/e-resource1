import { query, getMemoryDb } from '../../lib/db';
import { getAuthenticatedUser } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || !identity.worker_id) {
    return res.status(403).json({ message: 'Forbidden. Worker privileges required.' });
  }

  try {
    const worker_id = identity.worker_id;
    const sql = `
      SELECT w.*, u.username, u.email_id,
             COALESCE(AVG(r.rating), 0) as avg_rating,
             COUNT(r.review_id) as total_reviews
      FROM worker w
      JOIN user u ON u.worker_id = w.worker_id
      LEFT JOIN reviews r ON r.worker_id = w.worker_id
      WHERE w.worker_id = ?
      GROUP BY w.worker_id
    `;
    let result = await query(sql, [worker_id]);

    if (result.length === 0) {
      // Memory DB fallback check
      const db = getMemoryDb();
      const w = db.worker.find(wrk => wrk.worker_id === worker_id);
      if (w) {
        const u = db.user.find(usr => usr.worker_id === worker_id);
        const revs = db.reviews.filter(r => r.worker_id === worker_id);
        const total = revs.length;
        const avg = total > 0 ? revs.reduce((sum, r) => sum + r.rating, 0) / total : 0;
        result = [{
          ...w,
          username: u ? u.username : 'Worker',
          email_id: u ? u.email_id : 'worker@test.com',
          avg_rating: avg,
          total_reviews: total
        }];
      }
    }

    if (result.length === 0) {
      return res.status(404).json({ message: 'Worker not found' });
    }

    return res.status(200).json(result[0]);
  } catch (err) {
    console.error('Fetch worker self details error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
