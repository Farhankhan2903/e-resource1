import { query, getMemoryDb } from '../../lib/db';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  try {
    // Standard SQL to query verified workers and join with reviews for rating statistics
    const sql = `
      SELECT w.*, u.username, u.email_id,
             COALESCE(AVG(r.rating), 0) as avg_rating,
             COUNT(r.review_id) as total_reviews
      FROM worker w
      JOIN user u ON u.worker_id = w.worker_id
      LEFT JOIN reviews r ON r.worker_id = w.worker_id
      WHERE w.verified = 1
      GROUP BY w.worker_id
    `;
    
    let workers = await query(sql);

    // If memory fallback yields basic arrays, let's make sure avg_rating and total_reviews are calculated properly
    if (workers.length > 0 && workers[0].avg_rating === undefined) {
      const db = getMemoryDb();
      workers = workers.map(w => {
        const revs = db.reviews.filter(r => r.worker_id === w.worker_id);
        const total = revs.length;
        const avg = total > 0 ? revs.reduce((sum, r) => sum + r.rating, 0) / total : 0;
        return {
          ...w,
          avg_rating: avg,
          total_reviews: total
        };
      });
    }

    return res.status(200).json(workers);
  } catch (err) {
    console.error('Fetch workers error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
